package com.example

class DevbhashaApplication : android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        val check = com.google.firebase.appcheck.FirebaseAppCheck.getInstance()
        if (BuildConfig.DEBUG) {
            val factory = Class.forName("com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory")
                .getMethod("getInstance").invoke(null) as com.google.firebase.appcheck.AppCheckProviderFactory
            check.installAppCheckProviderFactory(factory)
        } else {
            check.installAppCheckProviderFactory(com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory.getInstance())
        }
    }
}
