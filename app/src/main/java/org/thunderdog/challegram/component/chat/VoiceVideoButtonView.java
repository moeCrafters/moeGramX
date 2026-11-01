/*
 * This file is a part of Telegram X
 * Copyright © 2014 (tgx-android@pm.me)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * File created on 10/11/2017
 */
package org.thunderdog.challegram.component.chat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

import org.thunderdog.challegram.R;
import org.thunderdog.challegram.navigation.MenuMoreWrap;
import org.thunderdog.challegram.config.Config;
import org.thunderdog.challegram.core.Lang;
import org.thunderdog.challegram.navigation.TooltipOverlayView;
import org.thunderdog.challegram.player.RecordAudioVideoController;
import org.thunderdog.challegram.theme.ColorId;
import org.thunderdog.challegram.theme.Theme;
import org.thunderdog.challegram.tool.Drawables;
import org.thunderdog.challegram.tool.Paints;
import org.thunderdog.challegram.tool.Screen;
import org.thunderdog.challegram.tool.UI;
import org.thunderdog.challegram.unsorted.Settings;
import org.thunderdog.challegram.util.HapticMenuHelper;

import java.util.List;

import me.vkryl.android.AnimatorUtils;
import me.vkryl.android.ViewUtils;
import me.vkryl.android.animator.BoolAnimator;
import me.vkryl.android.animator.FactorAnimator;
import me.vkryl.core.ColorUtils;
import me.vkryl.core.lambda.CancellableRunnable;
import me.vkryl.core.lambda.Destroyable;
import moe.kirao.mgx.MoexConfig;

public class VoiceVideoButtonView extends View implements FactorAnimator.Target, Settings.VideoModePreferenceListener, Destroyable, TooltipOverlayView.LocationProvider, HapticMenuHelper.Provider, HapticMenuHelper.OnItemClickListener, HapticMenuHelper.OnItemMenuListener {
  private boolean hasTouchControls;

  private final Drawable sendIcon, micIcon, videoIcon, searchIcon;
  private final HapticMenuHelper hapticMenuHelper;
  private boolean hapticMenuHandled;

  public VoiceVideoButtonView (Context context) {
    super(context);
    sendIcon = Drawables.get(getResources(), R.drawable.deproko_baseline_send_24);
    micIcon = Drawables.get(getResources(), R.drawable.deproko_baseline_msg_voice_24);
    videoIcon = Drawables.get(getResources(), R.drawable.deproko_baseline_msg_video_24);
    searchIcon = Drawables.get(getResources(), R.drawable.baseline_search_24);
    setInVideoMode(Settings.instance().preferVideoMode(), false);
    hapticMenuHelper = new HapticMenuHelper(this, this, null, null)
      .selectableMode(this)
      .hapticListener(this);
  }

  @Override
  public void getTargetBounds (View targetView, Rect outRect) {
    outRect.top += Screen.dp(8f);
    outRect.bottom -= Screen.dp(8f);
  }

  public void setHasTouchControls (boolean hasTouchControls) {
    if (this.hasTouchControls != hasTouchControls) {
      this.hasTouchControls = hasTouchControls;
      if (hasTouchControls) {
        Settings.instance().addVideoPreferenceChangeListener(this);
      } else {
        Settings.instance().removeVideoPreferenceChangeListener(this);
      }
      invalidate();
    }
  }

  private void startAndLockRecording (boolean selected) {
    RecordAudioVideoController controller = UI.getContext(getContext()).getRecordAudioVideoController();
    if (controller.startRecording(this, false, selected)) {
      this.draggingUp = true;
      this.draggingLeft = false;
      checkDraggingCircle();
      controller.setReleased(true, false);
      this.inLongTap = true;
      this.draggingUp = false;
      this.draggingLeft = false;
      checkDraggingCircle();
      controller.setTranslations(0, 0);
    } else {
      cancelLongTap = true;
    }
  }

  @Override
  public boolean onHapticMenuItemClick (View view, View parentView, HapticMenuHelper.MenuItem item) {
    hapticMenuHandled = true;
    startAndLockRecording(item.id == 0);
    return true;
  }

  @Override
  public int getAnchorMode (View view) {
    return MenuMoreWrap.ANCHOR_MODE_CENTER;
  }

  @Override
  public int getAnchorOffsetY (View view) {
    return Screen.dp(6f); // Ни разу не костыль
  }

  @Override
  public List<HapticMenuHelper.MenuItem> onCreateHapticMenu (View view) {
    if (!inVideoMode.getValue() || MoexConfig.instance().getRoundVideos() != MoexConfig.START_WITH_ASK) {
      return null;
    }
    return List.of(
      new HapticMenuHelper.MenuItem(0, Lang.getString(R.string.frontCamera), R.drawable.baseline_camera_front_24),
      new HapticMenuHelper.MenuItem(1, Lang.getString(R.string.RearCamera), R.drawable.baseline_camera_rear_24)
    );
  }

  private static final int ANIMATOR_VIDEO_MODE = 0, ANIMATOR_SEARCH_MODE = 1, ANIMATOR_HAPTIC_MENU = 2;
  private final BoolAnimator inVideoMode = new BoolAnimator(ANIMATOR_VIDEO_MODE, this, AnimatorUtils.DECELERATE_INTERPOLATOR, 120l);
  private final BoolAnimator inSearchMode = new BoolAnimator(ANIMATOR_SEARCH_MODE, this, AnimatorUtils.DECELERATE_INTERPOLATOR, 180l);
  private final BoolAnimator hapticMenuOpen = new BoolAnimator(ANIMATOR_HAPTIC_MENU, this, AnimatorUtils.DECELERATE_INTERPOLATOR, 180l);

  @Override
  public void onHapticMenuOpen () {
    hapticMenuOpen.setValue(true, true);
  }

  @Override
  public void onHapticMenuClose () {
    hapticMenuOpen.setValue(false, true);
  }

  @Override
  public void onPreferVideoModeChanged (boolean preferVideoMode) {
    setInVideoMode(preferVideoMode, hasTouchControls);
  }

  public void setInVideoMode (boolean inVideoMode, boolean animated) {
    this.inVideoMode.setValue(inVideoMode, animated);
  }

  public void setInSearchMode (boolean inSearchMode, boolean animated) {
    this.inSearchMode.setValue(inSearchMode, animated);
  }

  private float sendFactor;

  public void setSendFactor (float factor) {
    if (this.sendFactor != factor) {
      this.sendFactor = factor;
      invalidate();
    }
  }

  @Override
  public void onFactorChanged (int id, float factor, float fraction, FactorAnimator callee) {
    invalidate();
  }

  @Override
  public void onFactorChangeFinished (int id, float finalFactor, FactorAnimator callee) { }

  @Override
  public void performDestroy () {
    Settings.instance().removeVideoPreferenceChangeListener(this);
  }

  private Paint paint;

  private Paint getIconPaint () {
    int color = Theme.getColor(ColorId.circleButtonRegularIcon);
    if (paint == null || paint.getColor() != color)
      paint = Paints.createPorterDuffPaint(paint, color);
    return paint;
  }

  @Override
  protected void onDraw (Canvas c) {
    final int viewWidth = getMeasuredWidth();
    final int viewHeight = getMeasuredHeight();
    final int cx = getPaddingLeft() + (viewWidth - getPaddingLeft() - getPaddingRight()) / 2;
    final int cy = viewHeight / 2;
    final float videoFactor = inVideoMode.getFloatValue();
    final float searchFactor = inSearchMode.getFloatValue();
    final float hapticFactor = hapticMenuOpen.getFloatValue();

    if (hapticFactor > 0f) {
      float rad = Screen.dp(22.5f) * hapticFactor;
      c.drawCircle(cx, cy, rad, Paints.fillingPaint(ColorUtils.alphaColor(0.05f * hapticFactor, Theme.getColor(ColorId.text))));
    }
    final Paint paint = hasTouchControls ? Paints.getIconGrayPorterDuffPaint() : getIconPaint();
    final int savedAlpha = paint.getAlpha();
    final float generalFactor = (1f - sendFactor);
    if (generalFactor > 0f) {
      float alpha = generalFactor * (1f - searchFactor);
      if (alpha > 0f) {
        final float scale = Config.DEFAULT_ICON_SWITCH_SCALE + (1f - Config.DEFAULT_ICON_SWITCH_SCALE) * (1f - searchFactor);
        if (scale != 1f) {
          c.save();
          c.scale(scale, scale, cx, cy);
        }

        int y;

        y = (int) (cy + (cy + videoIcon.getMinimumHeight() / 2) * videoFactor);
        paint.setAlpha((int) ((float) savedAlpha * ((1f - videoFactor) * alpha)));
        Drawables.drawCentered(c, micIcon, cx, y, paint);

        y = (int) (cy - (cy + videoIcon.getMinimumHeight() / 2) * (1f - videoFactor));
        paint.setAlpha((int) ((float) savedAlpha * videoFactor * alpha));
        Drawables.drawCentered(c, videoIcon, cx, y, paint);

        if (scale != 1f) {
          c.restore();
        }
      }
      alpha = generalFactor * searchFactor;
      if (alpha > 0f) {
        final float scale = Config.DEFAULT_ICON_SWITCH_SCALE + (1f - Config.DEFAULT_ICON_SWITCH_SCALE) * searchFactor;
        if (scale != 1f) {
          c.save();
          c.scale(scale, scale, cx, cy);
        }

        paint.setAlpha((int) ((float) savedAlpha * alpha));
        Drawables.drawCentered(c, searchIcon, cx, cy, paint);

        if (scale != 1f) {
          c.restore();
        }
      }
    }

    paint.setAlpha((int) ((float) savedAlpha * sendFactor));
    Drawables.drawCentered(c, sendIcon, cx, cy, paint);

    paint.setAlpha(savedAlpha);
  }

  private float trackDownX, trackDownY;

  private boolean inLongTap;

  private boolean draggingUp, draggingLeft;

  private boolean isDown;

  private void setIsDown (boolean isDown) {
    if (this.isDown != isDown) {
      this.isDown = isDown;
      if (isDown) {
        scheduleLongTap();
      } else {
        cancelLongTap();
      }
    }
  }

  private CancellableRunnable scheduledLongTap;

  private void scheduleLongTap () {
    cancelLongTap();
    scheduledLongTap = new CancellableRunnable() {
      @Override
      public void act () {
        if (scheduledLongTap == this) {
          setInLongTap(true, false);
          scheduledLongTap = null;
        }
      }
    };
    postDelayed(scheduledLongTap, 120l);
  }

  private void cancelLongTap () {
    if (scheduledLongTap != null) {
      scheduledLongTap.cancel();
      scheduledLongTap = null;
    }
  }

  private void setInLongTap (boolean inLongTap, boolean byCancel) {
    if (this.inLongTap != inLongTap) {
      boolean success;
      if (inLongTap) {
        if (inVideoMode.getValue() && MoexConfig.instance().getRoundVideos() == MoexConfig.START_WITH_ASK) {
          hapticMenuHelper.openMenu(this);
          success = true;
        } else {
          success = UI.getContext(getContext()).getRecordAudioVideoController().startRecording(this, false, MoexConfig.instance().getRoundVideos() == MoexConfig.START_WITH_FRONT);
        }
      } else {
        if (!cancelLongTap) {
          UI.getContext(getContext()).getRecordAudioVideoController().finishRecording(byCancel);
        }
        success = true;
      }
      if (success) {
        this.inLongTap = inLongTap;
      } else {
        setIsDown(false);
      }
    }
  }

  private void performTap () {
    ViewUtils.onClick(this);
    Settings.instance().setPreferVideoMode(!inVideoMode.getValue());
  }

  private void checkDraggingCircle () {
    boolean isDragging = draggingUp && isDown;
    UI.getContext(getContext()).getRecordAudioVideoController().setApplyVerticalDrag(isDragging);
  }

  private boolean cancelLongTap;

  @Override
  public boolean onTouchEvent (MotionEvent e) {
    if (!hasTouchControls) {
      return false;// e.getAction() != MotionEvent.ACTION_DOWN || (getAlpha() > 0f && getVisibility() == View.VISIBLE);
    }

    switch (e.getAction()) {
      case MotionEvent.ACTION_DOWN: {
        trackDownX = e.getX();
        trackDownY = e.getY();
        draggingLeft = draggingUp = false;
        cancelLongTap = false;
        hapticMenuHandled = false;

        setIsDown(true);
        break;
      }
      case MotionEvent.ACTION_MOVE: {
        if (isDown && !hapticMenuHandled) {
          float x = e.getX();
          float y = e.getY();
          if (inLongTap && !(cancelLongTap || (cancelLongTap = !UI.getContext(getContext()).getRecordAudioVideoController().isOpen()))) {
            float diffX = Math.max(0f, (trackDownX - x));
            float diffY = Math.max(0f, (trackDownY - y));
            boolean draggingUp = diffY >= Screen.getTouchSlop() && diffY >= diffX - Screen.getTouchSlop();
            boolean draggingLeft = diffX >= Screen.getTouchSlop() && !draggingUp;
            if (!draggingLeft && !draggingUp) {
              draggingLeft = this.draggingLeft;
              draggingUp = this.draggingUp;
            }
            if (draggingLeft || draggingUp) {
              if (!this.draggingLeft && !this.draggingUp) {
                trackDownX = x;
                trackDownY = y;
                diffX = diffY = 0;
              }
              this.draggingLeft = draggingLeft;
              this.draggingUp = draggingUp;
              checkDraggingCircle();
              if (!UI.getContext(getContext()).getRecordAudioVideoController().setTranslations(-diffX, -diffY)) {
                cancelLongTap = true;
              }
            }
          } else {
            trackDownX = x;
            trackDownY = y;
          }
        }
        break;
      }
      case MotionEvent.ACTION_UP: {
        if (isDown) {
          if (hapticMenuHandled) {
            hapticMenuHandled = false;
          } else if (inLongTap) {
            setInLongTap(false, false);
          } else {
            performTap();
          }
          setIsDown(false);
        }
        break;
      }
      case MotionEvent.ACTION_CANCEL: {
        if (isDown) {
          if (!hapticMenuHandled) {
            setInLongTap(false, true);
          }
          hapticMenuHandled = false;
          setIsDown(false);
        }
        break;
      }
    }

    return true;
  }
}
