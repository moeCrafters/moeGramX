package moe.kirao.mgx.utils;

import android.content.Context;
import android.media.AudioManager;

import androidx.annotation.NonNull;

import org.thunderdog.challegram.Log;
import org.thunderdog.challegram.tool.UI;
import org.thunderdog.challegram.unsorted.AppContext;

import moe.kirao.mgx.MoexConfig;

public final class AudioFocusHelper {
  private final int mediaType;
  private final AudioManager.OnAudioFocusChangeListener listener;
  private boolean focusHeld;

  public AudioFocusHelper (int mediaType, @NonNull Runnable onFocusLost) {
    this.mediaType = mediaType;
    this.listener = focusChange -> {
      switch (focusChange) {
        case AudioManager.AUDIOFOCUS_LOSS:
        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
          if (focusHeld) {
            focusHeld = false;
            UI.post(onFocusLost);
          }
          break;
      }
    };
  }

  public void request () {
    if (focusHeld || !MoexConfig.shouldAutoPauseFor(mediaType)) {
      return;
    }
    AudioManager am = audioManager();
    if (am == null) {
      return;
    }
    int gainType = MoexConfig.autoPauseResumeSystemPlayback
      ? AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
      : AudioManager.AUDIOFOCUS_GAIN;
    try {
      am.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, gainType);
      focusHeld = true;
    } catch (Throwable t) {
      Log.e("Unable to request audio focus", t);
    }
  }

  public void abandon () {
    if (!focusHeld) {
      return;
    }
    focusHeld = false;
    AudioManager am = audioManager();
    if (am == null) {
      return;
    }
    try {
      am.abandonAudioFocus(listener);
    } catch (Throwable t) {
      Log.e("Unable to abandon audio focus", t);
    }
  }

  private static AudioManager audioManager () {
    Context ctx = AppContext.get();
    return ctx != null ? (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE) : null;
  }
}
