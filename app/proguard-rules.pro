# ==============================================================================
# Production ProGuard & R8 Obfuscation & Security Rules
# ==============================================================================

# General Obfuscation & Optimization
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Strip verbose debug logs in production release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# ==============================================================================
# Security & Crypto Packages
# ==============================================================================
-keep class com.example.security.** { *; }
-keepclassmembers class com.example.security.** {
    public *;
}
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# ==============================================================================
# Firebase Suite (Auth, Firestore, Messaging, Analytics, Functions, AI)
# ==============================================================================
-keepattributes *Annotation*
-dontwarn com.google.firebase.**
-keep class com.google.firebase.** { *; }
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.IgnoreExtraProperties class *;
}

# ==============================================================================
# OkHttp & Retrofit Network Security
# ==============================================================================
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes InnerClasses
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ==============================================================================
# Data Models & Serialization
# ==============================================================================
-keep class com.example.ui.models.** { *; }
-keepclassmembers class com.example.ui.models.** { *; }
-keep class com.example.data.** { *; }
-keepclassmembers class com.example.data.** { *; }
-keep class * implements java.io.Serializable { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable class *;
    *** Companion;
    *** serializer(...);
}

# ==============================================================================
# Razorpay Checkout SDK
# ==============================================================================
-dontwarn com.google.android.apps.nbu.paisa.inapp.client.api.**
-dontwarn proguard.annotation.Keep
-dontwarn proguard.annotation.KeepClassMembers
-dontwarn com.razorpay.**
-keep class com.razorpay.** { *; }
-optimizations !class/merging/vertical*,!class/merging/horizontal*
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ==============================================================================
# Agora Voice SDK
# ==============================================================================
-keep class io.agora.** { *; }
-dontwarn io.agora.**

# ==============================================================================
# Play Integrity API & Application Security Suite
# ==============================================================================
-keep class com.google.android.play.core.integrity.** { *; }
-dontwarn com.google.android.play.core.integrity.**
-keep class com.example.security.** { *; }
-keepclassmembers class com.example.security.** { *; }

# ==============================================================================
# Facebook SDK & AdServices Duplicate Class Handling
# ==============================================================================
-dontwarn com.facebook.**
-keep class com.facebook.** { *; }
-dontwarn android.adservices.topics.**

