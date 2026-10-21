package com.example.utils

import android.content.Context
import android.util.Log
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import com.example.BuildConfig

object AgoraVoiceManager {
    private const val TAG = "AgoraVoiceManager"

    var rtcEngine: RtcEngine? = null
        private set
    var isSimulationMode = true
        private set

    private var channel: String? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    var onError: ((Int) -> Unit)? = null
    var onRemoteJoined: (() -> Unit)? = null
    var onRemoteLeft: (() -> Unit)? = null
    var onJoined: (() -> Unit)? = null

    /**
     * @param serverAppId App ID returned by the `getAgoraToken` callable. Falls back to
     * BuildConfig only if the server didn't send one.
     */
    fun initEngine(context: Context, serverAppId: String? = null): Boolean {
        // App ID comes from the server (or config), never hardcoded in logic.
        val appId = serverAppId?.takeIf { it.isNotBlank() } ?: BuildConfig.AGORA_APP_ID

        if (!appId.trim().matches(Regex("^[0-9a-fA-F]{32}$"))) {
            Log.w(TAG, "Invalid Agora App ID — simulation mode")
            isSimulationMode = true
            return false
        }
        if (rtcEngine != null) return true
        return try {
            rtcEngine = RtcEngine.create(context, appId.trim(), object : IRtcEngineEventHandler() {
                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    handler.post { onJoined?.invoke() }
                }
                override fun onUserJoined(uid: Int, elapsed: Int) {
                    handler.post { onRemoteJoined?.invoke() }
                }
                override fun onUserOffline(uid: Int, reason: Int) {
                    handler.post { onRemoteLeft?.invoke() }
                }
                override fun onError(err: Int) { handler.post { onError?.invoke(err) } }
            })
            rtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            isSimulationMode = false
            true
        } catch (e: Exception) {
            Log.e(TAG, "Agora init failed", e)
            false
        }
    }

    /**
     * Join with a server-issued RTC token. Token, channel and uid must all come
     * from the `getAgoraToken` callable; joining without a token is not allowed.
     */
    fun join(channelName: String, token: String, uid: Int = 0): Boolean {
        val engine = rtcEngine ?: return false
        if (token.isBlank() || channelName.isBlank()) {
            Log.w(TAG, "Refusing to join Agora channel without a server-issued token")
            return false
        }
        val ok = engine.joinChannel(token, channelName.trim(), "", uid) == 0
        if (ok) channel = channelName.trim()
        return ok
    }

    fun joinOrRenew(channelName: String, token: String, uid: Int): Boolean {
        if (channel == channelName) return rtcEngine?.renewToken(token) == 0
        return join(channelName, token, uid)
    }

    fun leave() {
        channel = null
        try { rtcEngine?.leaveChannel() } catch (e: Exception) { }
    }

    fun setMuted(muted: Boolean) {
        try { rtcEngine?.muteLocalAudioStream(muted) } catch (e: Exception) { }
    }

    fun setSpeaker(on: Boolean) {
        try { rtcEngine?.setEnableSpeakerphone(on) } catch (e: Exception) { }
    }
}
