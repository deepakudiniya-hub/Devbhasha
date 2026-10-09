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
        return engine.joinChannel(token, channelName.trim(), "", uid) == 0
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
