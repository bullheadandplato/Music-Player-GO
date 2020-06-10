package com.iven.musicplayergo.player

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.bullhead.equalizer.DialogEqualizerFragment
import com.iven.musicplayergo.R
import com.iven.musicplayergo.extensions.toToast
import com.iven.musicplayergo.helpers.ThemeHelper

object EqualizerUtils {

    private fun hasEqualizer(context: Context): Boolean {
        val pm = context.packageManager
        val ri =
            pm.resolveActivity(Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL), 0)
        return ri != null
    }

    internal fun openAudioEffectSession(context: Context, sessionId: Int) {
        Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION).apply {
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId)
            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
            putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            context.sendBroadcast(this)
        }
    }

    internal fun closeAudioEffectSession(context: Context, sessionId: Int) {
        Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION).apply {
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId)
            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
            context.sendBroadcast(this)
        }
    }

    private fun openEqualizerDialog(
        context: Context,
        fragmentManager: FragmentManager,
        sessionId: Int
    ) {
        val accentColor = ThemeHelper.resolveThemeAccent(context)
        val alphaAccent = ColorUtils.setAlphaComponent(accentColor, 150)
        val fragment = DialogEqualizerFragment.newBuilder()
            .setAudioSessionId(sessionId)
            .themeColor(Color.parseColor("#212121"))
            .textColor(Color.WHITE)
            .accentAlpha(alphaAccent)
            .darkColor(Color.parseColor("#212121"))
            .setAccentColor(accentColor)
            .build()
        fragment.show(fragmentManager, "eq")
    }

    internal fun openEqualizer(
        activity: FragmentActivity,
        mediaPlayer: MediaPlayer,
        dialog: Boolean = false
    ) {
        if (dialog) {
            if (mediaPlayer.audioSessionId != AudioEffect.ERROR_BAD_VALUE) {
                openEqualizerDialog(
                    activity,
                    activity.supportFragmentManager,
                    mediaPlayer.audioSessionId
                )
            }
            return
        }
        if (hasEqualizer(activity))
            when (mediaPlayer.audioSessionId) {
                AudioEffect.ERROR_BAD_VALUE -> activity.getString(R.string.error_bad_id).toToast(
                    activity
                )
                else -> {
                    try {
                        Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                            putExtra(
                                AudioEffect.EXTRA_AUDIO_SESSION,
                                mediaPlayer.audioSessionId
                            )
                            putExtra(
                                AudioEffect.EXTRA_CONTENT_TYPE,
                                AudioEffect.CONTENT_TYPE_MUSIC
                            )
                            activity.startActivityForResult(this, 0)
                        }
                    } catch (notFound: ActivityNotFoundException) {
                        notFound.printStackTrace()
                    }
                }
            } else activity.getString(R.string.error_no_eq).toToast(activity)
    }
}
