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

    var onRemoteJoined: (() -> Unit)? = null
    var onRemoteLeft: (() -> Unit)? = null
    var onJoined: (() -> Unit)? = null

    fun initEngine(context: Context): Boolean {
        // AppID managed via server-side token generation
        val appId = "f13eed0b60d9479f88402de067953d90" 

        if (!appId.trim().matches(Regex("^[0-9a-fA-F]{32}$"))) {
            Log.w(TAG, "Invalid Agora App ID — simulation mode")
            isSimulationMode = true
            return false
        }
        if (rtcEngine != null) return true
        return try {
            rtcEngine = RtcEngine.create(context, appId.trim(), object : IRtcEngineEventHandler() {
                override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                    onJoined?.invoke()
                }
                override fun onUserJoined(uid: Int, elapsed: Int) {
                    onRemoteJoined?.invoke()
                }
                override fun onUserOffline(uid: Int, reason: Int) {
                    onRemoteLeft?.invoke()
                }
            })
            rtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
            isSimulationMode = false
            true
        } catch (e: Exception) {
            Log.e(TAG, "Agora init failed", e)
            false
        }
    }

    fun join(channelName: String, token: String): Boolean {
        val engine = rtcEngine ?: return false
        return engine.joinChannel(token, channelName.trim(), "", 0) == 0
    }

    fun leave() {
        try { rtcEngine?.leaveChannel() } catch (e: Exception) { }
    }

    fun setMuted(muted: Boolean) {
        try { rtcEngine?.muteLocalAudioStream(muted) } catch (e: Exception) { }
    }

    fun setSpeaker(on: Boolean) {
        try { rtcEngine?.setEnableSpeakerphone(on) } catch (e: Exception) { }
    }
}
