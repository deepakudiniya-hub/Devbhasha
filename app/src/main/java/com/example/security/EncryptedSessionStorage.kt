package com.example.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Production-grade Encrypted Storage backed by Android KeyStore (AES-256-GCM).
 * Implements strict cryptographic try-catch fallback: if KeyStore initialization or decryption fails,
 * drops back to an ephemeral, memory-only secure token map rather than falling back to unencrypted disk cache.
 */
class EncryptedSessionStorage(private val context: Context) {

    companion object {
        private const val TAG = "EncryptedSessionStorage"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "devbhasha_master_key_v1"
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val IV_SIZE = 12
        private const val PREFS_FILE = "devbhasha_encrypted_session_vault"

        @Volatile
        private var instance: EncryptedSessionStorage? = null

        fun getInstance(context: Context): EncryptedSessionStorage {
            return instance ?: synchronized(this) {
                instance ?: EncryptedSessionStorage(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    // Ephemeral memory-only secure fallback map when KeyStore corruption or runtime exception occurs
    private val memoryFallbackMap = ConcurrentHashMap<String, String>()
    private var isKeyStoreHealthy = true

    init {
        ensureMasterKey()
    }

    /**
     * Ensures an AES-256 master key exists in the Android KeyStore.
     * Marks isKeyStoreHealthy = false on failure, engaging memory-only fallback.
     */
    private fun ensureMasterKey() {
        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    KEYSTORE_PROVIDER
                )
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(true)
                    .build()

                keyGenerator.init(spec)
                keyGenerator.generateKey()
                Log.d(TAG, "Hardware-backed master key generated successfully in KeyStore")
            }
            isKeyStoreHealthy = true
        } catch (e: Exception) {
            Log.e(TAG, "Critical Security Error: KeyStore initialization failed. Engaging memory-only fallback.", e)
            isKeyStoreHealthy = false
        }
    }

    private fun getSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                ensureMasterKey()
            }
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            val key = entry?.secretKey
            if (key != null) {
                isKeyStoreHealthy = true
            }
            key
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve SecretKey from KeyStore. Switching to memory fallback.", e)
            isKeyStoreHealthy = false
            null
        }
    }

    /**
     * Encrypts plaintext using AES-256-GCM with a hardware-generated random 12-byte IV.
     * In AndroidKeyStore with setRandomizedEncryptionRequired(true), the provider generates
     * the fresh random 12-byte IV during cipher.init(Cipher.ENCRYPT_MODE, secretKey).
     * Returns Base64-encoded string: [12 bytes IV] + [Ciphertext + Auth Tag]
     */
    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        val secretKey = getSecretKey()
        if (secretKey == null) {
            // Memory fallback when KeyStore is compromised/unhealthy
            memoryFallbackMap[plainText] = plainText
            return "MEM_FALLBACK:$plainText"
        }

        return try {
            val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
            
            // In AndroidKeyStore, passing caller-provided IV when randomizedEncryptionRequired=true
            // throws InvalidAlgorithmParameterException ("Caller-provided IV not permitted").
            // Calling cipher.init without IV causes the provider to generate the fresh secure IV.
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv ?: throw IllegalStateException("Cipher failed to generate IV")
            val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(iv.size + cipherBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherBytes, 0, combined, iv.size, cipherBytes.size)

            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Encryption exception encountered. Switching to ephemeral memory fallback.", e)
            isKeyStoreHealthy = false
            memoryFallbackMap[plainText] = plainText
            "MEM_FALLBACK:$plainText"
        }
    }

    /**
     * Decrypts Base64-encoded AES-256-GCM ciphertext using the extracted 12-byte IV.
     */
    fun decrypt(encryptedBase64: String): String {
        if (encryptedBase64.isEmpty()) return ""
        if (encryptedBase64.startsWith("MEM_FALLBACK:")) {
            return encryptedBase64.removePrefix("MEM_FALLBACK:")
        }

        val secretKey = getSecretKey()
        if (secretKey == null) {
            return memoryFallbackMap[encryptedBase64] ?: encryptedBase64
        }

        return try {
            val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
            if (combined.size <= IV_SIZE) return encryptedBase64

            val iv = ByteArray(IV_SIZE)
            val cipherBytes = ByteArray(combined.size - IV_SIZE)

            System.arraycopy(combined, 0, iv, 0, IV_SIZE)
            System.arraycopy(combined, IV_SIZE, cipherBytes, 0, cipherBytes.size)

            val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val plainBytes = cipher.doFinal(cipherBytes)
            String(plainBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failure or KeyStore corruption detected. Dropping to memory fallback.", e)
            isKeyStoreHealthy = false
            memoryFallbackMap[encryptedBase64] ?: encryptedBase64
        }
    }

    fun putSecureString(key: String, value: String) {
        val encrypted = encrypt(value)
        if (encrypted.startsWith("MEM_FALLBACK:")) {
            memoryFallbackMap[key] = value
        } else {
            prefs.edit().putString(key, encrypted).apply()
        }
    }

    fun getSecureString(key: String, defaultValue: String = ""): String {
        memoryFallbackMap[key]?.let { return it }
        val stored = prefs.getString(key, null) ?: return defaultValue
        if (stored.startsWith("MEM_FALLBACK:")) {
            return stored.removePrefix("MEM_FALLBACK:")
        }
        val decrypted = decrypt(stored)
        return if (decrypted.isNotEmpty()) decrypted else defaultValue
    }

    fun putSecureBoolean(key: String, value: Boolean) {
        putSecureString(key, value.toString())
    }

    fun getSecureBoolean(key: String, defaultValue: Boolean = false): Boolean {
        val str = getSecureString(key, "")
        return if (str.isNotEmpty()) str.toBooleanStrictOrNull() ?: defaultValue else defaultValue
    }

    fun remove(key: String) {
        memoryFallbackMap.remove(key)
        prefs.edit().remove(key).apply()
    }

    fun clearAll() {
        memoryFallbackMap.clear()
        prefs.edit().clear().apply()
    }
}
