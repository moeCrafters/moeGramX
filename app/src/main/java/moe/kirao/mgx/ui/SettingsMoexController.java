package moe.kirao.mgx.ui;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import org.thunderdog.challegram.R;
import org.thunderdog.challegram.U;
import org.thunderdog.challegram.component.base.SettingView;
import org.thunderdog.challegram.config.Config;
import org.thunderdog.challegram.core.Lang;
import org.thunderdog.challegram.navigation.SettingsWrapBuilder;
import org.thunderdog.challegram.telegram.Tdlib;
import org.thunderdog.challegram.telegram.TdlibUi;
import org.thunderdog.challegram.theme.ColorId;
import org.thunderdog.challegram.tool.UI;
import org.thunderdog.challegram.ui.ListItem;
import org.thunderdog.challegram.ui.RecyclerViewController;
import org.thunderdog.challegram.ui.SettingsAdapter;
import org.thunderdog.challegram.unsorted.Settings;
import org.thunderdog.challegram.v.CustomRecyclerView;

import java.util.ArrayList;

import moe.kirao.mgx.MoexConfig;

public class SettingsMoexController extends RecyclerViewController<SettingsMoexController.Args> implements View.OnClickListener, View.OnLongClickListener {
  public SettingsMoexController (Context context, Tdlib tdlib) {
    super(context, tdlib);
  }

  @Override public int getId () {
    return R.id.controller_moexSettings;
  }

  public static final int CATEGORY_GENERAL = 1;
  public static final int CATEGORY_INTERFACE = 2;
  public static final int CATEGORY_CHATS = 3;
  public static final int CATEGORY_MISC = 4;

  private int category;

  public static class Args {
    private final int category;

    public Args (int category) {
      this.category = category;
    }
  }

  @Override
  public void setArguments (Args args) {
    super.setArguments(args);
    this.category = args.category;
  }

  @Override public CharSequence getName () {
    return category == CATEGORY_GENERAL
      ? Lang.getString(R.string.GeneralMoexSettings) : category == CATEGORY_CHATS
      ? Lang.getString(R.string.Chats) : category == CATEGORY_INTERFACE
      ? Lang.getString(R.string.InterfaceMoexSettings) : category == CATEGORY_MISC
      ? Lang.getString(R.string.Other) : Lang.getString(R.string.MoexSettings);
  }

  private SettingsAdapter adapter;

  @Override public void onClick (View v) {
    int viewId = v.getId();
    SettingsMoexController c = new SettingsMoexController(context, tdlib);

    if (viewId == R.id.btn_GeneralMoexSettings) {
      c.setArguments(new SettingsMoexController.Args(SettingsMoexController.CATEGORY_GENERAL));
      navigateTo(c);
    } else if (viewId == R.id.btn_InterfaceMoexSettings) {
      c.setArguments(new SettingsMoexController.Args(SettingsMoexController.CATEGORY_INTERFACE));
      navigateTo(c);
    } else if (viewId == R.id.btn_ChatsMoexSettings) {
      c.setArguments(new SettingsMoexController.Args(SettingsMoexController.CATEGORY_CHATS));
      navigateTo(c);
    } else if (viewId == R.id.btn_MiscMoexSettings) {
      c.setArguments(new SettingsMoexController.Args(SettingsMoexController.CATEGORY_MISC));
      navigateTo(c);
    } else if (viewId == R.id.btn_moexCrowdinLink) {
      tdlib.ui().openUrl(this, Lang.getString(R.string.MoexCrowdinLink), new TdlibUi.UrlOpenParameters());
    } else if (viewId == R.id.btn_moexChatLink) {
      tdlib.ui().openUrl(this, Lang.getString(R.string.MoexChatLink), new TdlibUi.UrlOpenParameters().forceInstantView());
    } else if (viewId == R.id.btn_moexChannelLink) {
      tdlib.ui().openUrl(this, Lang.getString(R.string.MoexChannelLink), new TdlibUi.UrlOpenParameters().forceInstantView());
    } else if (viewId == R.id.btn_moexSourceLink) {
      tdlib.ui().openUrl(this, Lang.getString(R.string.MoexSourceLink), new TdlibUi.UrlOpenParameters());
    } else if (viewId == R.id.btn_build) {
      UI.showToast(R.string.cuteToast, Toast.LENGTH_SHORT);
    } else if (viewId == R.id.btn_hidePhone) {
      MoexConfig.instance().toggleHidePhoneNumber();
      adapter.updateValuedSettingById(viewId);
    } else if (viewId == R.id.btn_enableFeaturesButton) {
      MoexConfig.instance().toggleEnableFeaturesButton();
      adapter.updateValuedSettingById(viewId);
    } else if (viewId == R.id.btn_showIdProfile) {
      Settings.instance().setExperimentEnabled(Settings.EXPERIMENT_FLAG_SHOW_PEER_IDS, adapter.toggleView(v));
    } else if (viewId == R.id.btn_hideMessagesBadge) {
      MoexConfig.instance().toggleHideMessagesBadge();
      adapter.updateValuedSettingById(viewId);
    } else if (viewId == R.id.btn_changeSizeLimit) {
      showChangeSizeLimit();
    } else if (viewId == R.id.btn_squareAvatar) {
      MoexConfig.instance().toggleSquareAvatar();
      adapter.updateValuedSettingById(viewId);
    } else if (viewId == R.id.btn_blurDrawer) {
      MoexConfig.instance().toggleBlurDrawer();
      adapter.updateValuedSettingById(viewId);
    } else if (viewId == R.id.btn_autoPauseMedia) {
      showAutoPauseTypeOptions();
    } else if (viewId == R.id.btn_headerText) {
      showHeaderTextOptions();
    } else if (viewId == R.id.btn_disableReactions) {
      MoexConfig.instance().toggleDisableReactions();
      adapter.updateValuedSettingById(viewId);
    } else if (viewId == R.id.btn_hideMessagePanelButtons) {
      showHideMessagePanelOptions();
    } else if (viewId == R.id.btn_hideBottomBar) {
      MoexConfig.instance().toggleHideBottomBar();
      adapter.updateValuedSettingById(viewId);
    } else if (viewId == R.id.btn_disableStickerTimestamp) {
      MoexConfig.instance().toggleDisableStickerTimestamp();
      adapter.updateValuedSettingById(viewId);
    } else if (viewId == R.id.btn_roundedStickers) {
      MoexConfig.instance().toggleRoundedStickers();
      adapter.updateValuedSettingById(viewId);
    } else if (viewId == R.id.btn_IncreaseRecents) {
      MoexConfig.instance().toggleIncreaseRecents();
      adapter.updateValuedSettingById(viewId);
    } else if (viewId == R.id.btn_rememberOptions) {
      MoexConfig.instance().toggleRememberSendOptions();
      adapter.updateValuedSettingById(viewId);
    } else if (viewId == R.id.btn_typingInstead) {
      MoexConfig.instance().toggleTypingInsteadChoosing();
      adapter.updateValuedSettingById(viewId);
    } else if (viewId == R.id.btn_darkenDrawer) {
      MoexConfig.instance().toggleDarkenDrawer();
      adapter.updateValuedSettingById(viewId);
    } else if (viewId == R.id.btn_silent) {
      MoexConfig.instance().toggleSilentMessage();
      adapter.updateValuedSettingById(viewId);
    } else if (viewId == R.id.btn_rearRounds) {
      showRoundVideoCameraOptions();
    } else if (viewId == R.id.btn_toggleEdgeAnimSide) {
      handleSettingClick(v, adapter);
    }
  }

  @Override
  public boolean onLongClick (View v) {
    if (v.getId() == R.id.btn_build) {
      UI.copyText(String.join("\n", Lang.getStringSecure(R.string.MoexVer), U.getUsefulMetadata(tdlib)), R.string.CopiedText);
    }
    return false;
  }

  private void showChangeSizeLimit () {
    int sizeLimitOption = MoexConfig.instance().getSizeLimit();
    showSettings(new SettingsWrapBuilder(R.id.btn_changeSizeLimit).setRawItems(new ListItem[] {
      new ListItem(ListItem.TYPE_RADIO_OPTION, R.id.btn_sizeLimit800, 0, R.string.px800, R.id.btn_changeSizeLimit, sizeLimitOption == MoexConfig.SIZE_LIMIT_800),
      new ListItem(ListItem.TYPE_RADIO_OPTION, R.id.btn_sizeLimit1280, 0, R.string.px1280, R.id.btn_changeSizeLimit, sizeLimitOption == MoexConfig.SIZE_LIMIT_1280),
      new ListItem(ListItem.TYPE_RADIO_OPTION, R.id.btn_sizeLimit2560, 0, R.string.px2560, R.id.btn_changeSizeLimit, sizeLimitOption == MoexConfig.SIZE_LIMIT_2560),
    }).setAllowResize(false).addHeaderItem(Lang.getMarkdownString(this, R.string.SizeLimitDesc)).setIntDelegate((id, result) -> {
      int sizeLimit = result.get(R.id.btn_changeSizeLimit);
      int sizeOption;
      if (sizeLimit == R.id.btn_sizeLimit800) {
        sizeOption = MoexConfig.SIZE_LIMIT_800;
      } else if (sizeLimit == R.id.btn_sizeLimit1280) {
        sizeOption = MoexConfig.SIZE_LIMIT_1280;
      } else {
        sizeOption = MoexConfig.SIZE_LIMIT_2560;
      }

      MoexConfig.instance().setSizeLimit(sizeOption);
      adapter.updateValuedSettingById(R.id.btn_changeSizeLimit);
    }));
  }

  private void showHideMessagePanelOptions () {
    showSettings(new SettingsWrapBuilder(R.id.btn_hideMessagePanelButtons).addHeaderItem(
      new ListItem(ListItem.TYPE_INFO, R.id.text_title, 0, R.string.HideCameraButtonInfo, false)).setRawItems(
      new ListItem[] {
        new ListItem(ListItem.TYPE_CHECKBOX_OPTION, R.id.btn_disableCameraButton, 0, R.string.DisableCameraButton, MoexConfig.disableCameraButton),
        new ListItem(ListItem.TYPE_CHECKBOX_OPTION, R.id.btn_disableCommandsButton, 0, R.string.DisableCommandsButton, MoexConfig.disableCommandsButton),
        new ListItem(ListItem.TYPE_CHECKBOX_OPTION, R.id.btn_disableRecordButton, 0, R.string.DisableRecordButton, MoexConfig.disableRecordButton),
        new ListItem(ListItem.TYPE_CHECKBOX_OPTION, R.id.btn_disableSendAsButton, 0, R.string.DisableSendAsButton, MoexConfig.disableSendAsButton)
      }).setIntDelegate((id, result) -> {
      if (MoexConfig.disableCameraButton == (result.get(R.id.btn_disableCameraButton) == 0)) {
        MoexConfig.instance().toggleDisableCameraButton();
      }
      if (MoexConfig.disableCommandsButton == (result.get(R.id.btn_disableCommandsButton) == 0)) {
        MoexConfig.instance().toggleDisableCommandsButton();
      }
      if (MoexConfig.disableRecordButton == (result.get(R.id.btn_disableRecordButton) == 0)) {
        MoexConfig.instance().toggleDisableRecordButton();
      }
      if (MoexConfig.disableSendAsButton == (result.get(R.id.btn_disableSendAsButton) == 0)) {
        MoexConfig.instance().toggleDisableSendAsButton();
      }

      adapter.updateValuedSettingById(R.id.btn_hideMessagePanelButtons);
    }));
  }

  private void showHeaderTextOptions () {
    int headerTextOption = MoexConfig.instance().getHeaderText();
    showSettings(new SettingsWrapBuilder(R.id.btn_headerText).setRawItems(new ListItem[] {
      new ListItem(ListItem.TYPE_RADIO_OPTION, R.id.btn_headerTextChats, 0, R.string.Chats, R.id.btn_headerText, headerTextOption == MoexConfig.HEADER_TEXT_CHATS),
      new ListItem(ListItem.TYPE_RADIO_OPTION, R.id.btn_headerTextMoex, 0, R.string.moexHeaderClient, R.id.btn_headerText, headerTextOption == MoexConfig.HEADER_TEXT_MOEX),
      new ListItem(ListItem.TYPE_RADIO_OPTION, R.id.btn_headerTextUsername, 0, R.string.Username, R.id.btn_headerText, headerTextOption == MoexConfig.HEADER_TEXT_USERNAME),
      new ListItem(ListItem.TYPE_RADIO_OPTION, R.id.btn_headerTextName, 0, R.string.login_FirstName, R.id.btn_headerText, headerTextOption == MoexConfig.HEADER_TEXT_NAME),
    }).setIntDelegate((id, result) -> {
      int headerText = result.get(R.id.btn_headerText);
      int defaultOption;
      if (headerText == R.id.btn_headerTextChats) {
        defaultOption = MoexConfig.HEADER_TEXT_CHATS;
      } else if (headerText == R.id.btn_headerTextName) {
        defaultOption = MoexConfig.HEADER_TEXT_NAME;
      } else if (headerText == R.id.btn_headerTextUsername) {
        defaultOption = MoexConfig.HEADER_TEXT_USERNAME;
      } else {
        defaultOption = MoexConfig.HEADER_TEXT_MOEX;
      }

      MoexConfig.instance().setHeaderText(defaultOption);
      adapter.updateValuedSettingById(R.id.btn_headerText);
    }));
  }

  private void showAutoPauseTypeOptions () {
    int flags = MoexConfig.instance().getAutoPauseMediaTypes();
    showSettings(new SettingsWrapBuilder(R.id.btn_autoPauseMedia).setRawItems(new ListItem[] {
      new ListItem(ListItem.TYPE_CHECKBOX_OPTION, R.id.btn_autoPauseResume, 0, R.string.AutoPauseMediaResume, R.id.btn_autoPauseResume, MoexConfig.autoPauseResumeSystemPlayback),
      new ListItem(ListItem.TYPE_SHADOW_BOTTOM).setTextColorId(ColorId.background),
      new ListItem(ListItem.TYPE_SHADOW_TOP).setTextColorId(ColorId.background),
      new ListItem(ListItem.TYPE_CHECKBOX_OPTION, R.id.autoPauseTypeVoice, 0, R.string.VoiceMessages, R.id.autoPauseTypeVoice, (flags & MoexConfig.AUTO_PAUSE_MEDIA_VOICE) != 0),
      new ListItem(ListItem.TYPE_SEPARATOR_FULL),
      new ListItem(ListItem.TYPE_CHECKBOX_OPTION, R.id.autoPauseTypeRound, 0, R.string.VideoMessages, R.id.autoPauseTypeRound, (flags & MoexConfig.AUTO_PAUSE_MEDIA_ROUND) != 0),
      new ListItem(ListItem.TYPE_SEPARATOR_FULL),
      new ListItem(ListItem.TYPE_CHECKBOX_OPTION, R.id.autoPauseTypeVideo, 0, R.string.Videos, R.id.autoPauseTypeVideo, (flags & MoexConfig.AUTO_PAUSE_MEDIA_VIDEO) != 0),
    }).setNeedSeparators(false).setIntDelegate((id, result) -> {
      int newFlags = 0;
      if (result.get(R.id.autoPauseTypeVoice) == R.id.autoPauseTypeVoice) newFlags |= MoexConfig.AUTO_PAUSE_MEDIA_VOICE;
      if (result.get(R.id.autoPauseTypeRound) == R.id.autoPauseTypeRound) newFlags |= MoexConfig.AUTO_PAUSE_MEDIA_ROUND;
      if (result.get(R.id.autoPauseTypeVideo) == R.id.autoPauseTypeVideo) newFlags |= MoexConfig.AUTO_PAUSE_MEDIA_VIDEO;
      MoexConfig.instance().setAutoPauseMediaTypes(newFlags);
      MoexConfig.instance().setAutoPauseResumeSystemPlayback(result.get(R.id.btn_autoPauseResume) == R.id.btn_autoPauseResume);
      adapter.updateValuedSettingById(R.id.btn_autoPauseMedia);
    }));
  }

  private void showRoundVideoCameraOptions () {
    int selected = MoexConfig.instance().getRoundVideos();
    showSettings(new SettingsWrapBuilder(R.id.btn_rearRounds).setRawItems(new ListItem[] {
      new ListItem(ListItem.TYPE_RADIO_OPTION, R.id.frontRoundVideo, 0, R.string.frontCamera, R.id.btn_rearRounds, selected == MoexConfig.START_WITH_FRONT),
      new ListItem(ListItem.TYPE_RADIO_OPTION, R.id.rearRoundVideo, 0, R.string.RearCamera, R.id.btn_rearRounds, selected == MoexConfig.START_WITH_REAR),
      new ListItem(ListItem.TYPE_RADIO_OPTION, R.id.askRoundVideo, 0, R.string.Ask, R.id.btn_rearRounds, selected == MoexConfig.START_WITH_ASK),
    }).setIntDelegate((id, result) -> {
      int newMode = selected;
      final int resultId = result.get(R.id.btn_rearRounds);
      if (resultId == R.id.frontRoundVideo) {
        newMode = MoexConfig.START_WITH_FRONT;
      } else if (resultId == R.id.rearRoundVideo) {
        newMode = MoexConfig.START_WITH_REAR;
      } else if (resultId == R.id.askRoundVideo) {
        newMode = MoexConfig.START_WITH_ASK;
      }
      MoexConfig.instance().setRoundVideos(newMode);
      adapter.updateValuedSettingById(R.id.btn_rearRounds);
    }));
  }

  @Override
  protected void onCreateView (Context context, CustomRecyclerView recyclerView) {
    adapter = new SettingsAdapter(this) {
      @Override
      protected void setValuedSetting (ListItem item, SettingView view, boolean isUpdate) {
        view.setDrawModifier(item.getDrawModifier());
        int itemId = item.getId();

        if (itemId == R.id.btn_moexCrowdinLink) {
          view.setData(R.string.MoexCrowdinText);
        } else if (itemId == R.id.btn_moexChatLink) {
          view.setData(R.string.moexChat);
        } else if (itemId == R.id.btn_moexChannelLink) {
          view.setData(R.string.moexChannel);
        } else if (itemId == R.id.btn_moexSourceLink) {
          view.setData(R.string.moexGithub);
        } else if (itemId == R.id.btn_hidePhone) {
          view.getToggler().setRadioEnabled(MoexConfig.hidePhoneNumber, isUpdate);
        } else if (itemId == R.id.btn_enableFeaturesButton) {
          view.getToggler().setRadioEnabled(MoexConfig.enableTestFeatures, isUpdate);
        } else if (itemId == R.id.btn_showIdProfile) {
          view.getToggler().setRadioEnabled(Settings.instance().isExperimentEnabled(Settings.EXPERIMENT_FLAG_SHOW_PEER_IDS), isUpdate);
        } else if (itemId == R.id.btn_hideMessagesBadge) {
          view.getToggler().setRadioEnabled(MoexConfig.hideMessagesBadge, isUpdate);
        } else if (itemId == R.id.btn_changeSizeLimit) {
          int size = MoexConfig.instance().getSizeLimit();
          switch (size) {
            case MoexConfig.SIZE_LIMIT_800:
              view.setData(R.string.px800);
              break;
            case MoexConfig.SIZE_LIMIT_1280:
              view.setData(R.string.px1280);
              break;
            case MoexConfig.SIZE_LIMIT_2560:
              view.setData(R.string.px2560);
              break;
          }
        } else if (itemId == R.id.btn_disableCameraButton) {
          view.getToggler().setRadioEnabled(MoexConfig.disableCameraButton, isUpdate);
        } else if (itemId == R.id.btn_disableRecordButton) {
          view.getToggler().setRadioEnabled(MoexConfig.disableRecordButton, isUpdate);
        } else if (itemId == R.id.btn_disableCommandsButton) {
          view.getToggler().setRadioEnabled(MoexConfig.disableCommandsButton, isUpdate);
        } else if (itemId == R.id.btn_disableSendAsButton) {
          view.getToggler().setRadioEnabled(MoexConfig.disableSendAsButton, isUpdate);
        } else if (itemId == R.id.btn_squareAvatar) {
          view.getToggler().setRadioEnabled(MoexConfig.squareAvatar, isUpdate);
        } else if (itemId == R.id.btn_blurDrawer) {
          view.getToggler().setRadioEnabled(MoexConfig.blurDrawer, isUpdate);
        } else if (itemId == R.id.btn_autoPauseMedia) {
          int flags = MoexConfig.instance().getAutoPauseMediaTypes();
          view.getToggler().setRadioEnabled(flags != 0, isUpdate);
          if (flags == MoexConfig.AUTO_PAUSE_MEDIA_ALL) {
            view.setData(R.string.AllMedia);
          } else if (flags == 0) {
            view.setData(R.string.Off);
          } else {
            StringBuilder sb = new StringBuilder();
            if ((flags & MoexConfig.AUTO_PAUSE_MEDIA_VOICE) != 0)
              sb.append(Lang.getString(R.string.VoiceMessages));
            if ((flags & MoexConfig.AUTO_PAUSE_MEDIA_ROUND) != 0)
              sb.append(sb.length() > 0 ? ", " : "").append(Lang.getString(R.string.VideoMessages));
            if ((flags & MoexConfig.AUTO_PAUSE_MEDIA_VIDEO) != 0)
              sb.append(sb.length() > 0 ? ", " : "").append(Lang.getString(R.string.Videos));
            view.setData(sb.toString());
          }
        } else if (itemId == R.id.btn_headerText) {
          int mode = MoexConfig.instance().getHeaderText();
          switch (mode) {
            case MoexConfig.HEADER_TEXT_CHATS:
              view.setData(R.string.Chats);
              break;
            case MoexConfig.HEADER_TEXT_MOEX:
              view.setData(R.string.moexHeaderClient);
              break;
            case MoexConfig.HEADER_TEXT_USERNAME:
              view.setData(R.string.Username);
              break;
            case MoexConfig.HEADER_TEXT_NAME:
              view.setData(R.string.login_FirstName);
              break;
          }
        } else if (itemId == R.id.btn_disableReactions) {
          view.getToggler().setRadioEnabled(MoexConfig.disableReactions, isUpdate);
        } else if (itemId == R.id.btn_hideMessagePanelButtons) {
          StringBuilder b = new StringBuilder();
          String separator = Lang.getConcatSeparator();

          if (MoexConfig.disableCameraButton) {
            if (b.length() > 0) b.append(separator);
            b.append(Lang.getString(R.string.DisableCameraButton));
          }
          if (MoexConfig.disableRecordButton) {
            if (b.length() > 0) b.append(separator);
            b.append(Lang.getString(R.string.DisableRecordButton));
          }
          if (MoexConfig.disableCommandsButton) {
            if (b.length() > 0) b.append(separator);
            b.append(Lang.getString(R.string.DisableCommandsButton));
          }
          if (MoexConfig.disableSendAsButton) {
            if (b.length() > 0) b.append(separator);
            b.append(Lang.getString(R.string.DisableSendAsButton));
          }

          view.setData(b.length() == 0 ? Lang.getString(R.string.BioNone) : b.toString());
        } else if (itemId == R.id.btn_hideBottomBar) {
          view.getToggler().setRadioEnabled(MoexConfig.hideBottomBar, isUpdate);
        } else if (itemId == R.id.btn_disableStickerTimestamp) {
          view.getToggler().setRadioEnabled(MoexConfig.hideStickerTimestamp, isUpdate);
        } else if (itemId == R.id.btn_roundedStickers) {
          view.getToggler().setRadioEnabled(MoexConfig.roundedStickers, isUpdate);
        } else if (itemId == R.id.btn_IncreaseRecents) {
          view.getToggler().setRadioEnabled(MoexConfig.increaseRecents, isUpdate);
        } else if (itemId == R.id.btn_rememberOptions) {
          view.getToggler().setRadioEnabled(MoexConfig.rememberOptions, isUpdate);
        } else if (itemId == R.id.btn_typingInstead) {
          view.getToggler().setRadioEnabled(MoexConfig.typingInsteadChoosing, isUpdate);
        } else if (itemId == R.id.btn_darkenDrawer) {
          view.getToggler().setRadioEnabled(MoexConfig.darkenDrawer, isUpdate);
        } else if (itemId == R.id.btn_silent) {
          view.getToggler().setRadioEnabled(MoexConfig.silentMessage, isUpdate);
        } else if (itemId == R.id.btn_toggleEdgeAnimSide) {
          updateSettingView(view, item, isUpdate);
        } else if (itemId == R.id.btn_rearRounds) {
          switch (MoexConfig.instance().getRoundVideos()) {
            case MoexConfig.START_WITH_FRONT:
              view.setData(R.string.frontCamera);
              break;
            case MoexConfig.START_WITH_REAR:
              view.setData(R.string.RearCamera);
              break;
            case MoexConfig.START_WITH_ASK:
              view.setData(R.string.Ask);
              break;
          }
        }
      }
    };

    ArrayList<ListItem> items = new ArrayList<>();
    items.add(new ListItem(ListItem.TYPE_EMPTY_OFFSET_SMALL));

    switch (category) {
      case CATEGORY_GENERAL:
        items.add(new ListItem(ListItem.TYPE_HEADER, 0, 0, R.string.GeneralMoexSettings));
        items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));
        items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_hideMessagesBadge, 0, R.string.hideMessagesBadge));
        items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
        items.add(new ListItem(ListItem.TYPE_VALUED_SETTING_COMPACT, R.id.btn_headerText, 0, R.string.changeHeaderText));
        if (Config.EDGE_TO_EDGE_AVAILABLE) {
          items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
          items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_toggleEdgeAnimSide, 0, R.string.RightSwipeEdgeAnimation).setLongId(Settings.SETTING_FLAG_FORCE_DEFAULT_ANIMATION_FOR_RIGHT_SWIPE_EDGE).setBoolValue(true));
        }
        items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));
        if (Config.EDGE_TO_EDGE_AVAILABLE) {
          items.add(new ListItem(ListItem.TYPE_DESCRIPTION, 0, 0, R.string.RightSwipeEdgeAnimationInfo));
        }

        items.add(new ListItem(ListItem.TYPE_HEADER, 0, 0, R.string.ProfileOptions));
        items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));
        items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_showIdProfile, 0, R.string.showIdProfile));
        items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
        items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_hidePhone, 0, R.string.hidePhoneNumber));
        items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));
        break;
      case CATEGORY_INTERFACE:
        items.add(new ListItem(ListItem.TYPE_HEADER, 0, 0, R.string.DrawerOptions));
        items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));
        items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_blurDrawer, 0, R.string.MoexBlurDrawer));
        items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
        items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_darkenDrawer, 0, R.string.MoexDarkenDrawer));
        items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));

        items.add(new ListItem(ListItem.TYPE_HEADER, 0, 0, R.string.MoexChatsHeader));
        items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));
        items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_squareAvatar, 0, R.string.SquareAvatar));
        items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
        items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_disableReactions, 0, R.string.DisableReactions));
        items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));

        items.add(new ListItem(ListItem.TYPE_HEADER, 0, 0, R.string.MoexHideButtons));
        items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));
        items.add(new ListItem(ListItem.TYPE_VALUED_SETTING_COMPACT, R.id.btn_hideMessagePanelButtons, 0, R.string.HideMessagePanelButtons));
        items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
        items.add(new ListItem(ListItem.TYPE_VALUED_SETTING_COMPACT, R.id.btn_rearRounds, 0, R.string.SelectRoundVideos));
        items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
        items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_hideBottomBar, 0, R.string.HideBottomBar));
        break;
      case CATEGORY_CHATS:
        items.add(new ListItem(ListItem.TYPE_HEADER, 0, 0, R.string.MoexStickersCount));
        items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));
        items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_disableStickerTimestamp, 0, R.string.DisableStickerTimestamp));
        items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
        items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_roundedStickers, 0, R.string.RoundedStickers));
        items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
        items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_IncreaseRecents, 0, R.string.IncreaseRecents));
        items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));

        items.add(new ListItem(ListItem.TYPE_HEADER, 0, 0, R.string.ActivityOptions));
        items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));
        items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_rememberOptions, 0, R.string.RememberOptions));
        items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));
        items.add(new ListItem(ListItem.TYPE_DESCRIPTION, 0, 0, Lang.getMarkdownString(this, R.string.RememberOptionsInfo), false));
        items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));
        items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_silent, 0, R.string.SilentMessage));
        items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
        items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_typingInstead, 0, R.string.TypingInstead));
        items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));
        items.add(new ListItem(ListItem.TYPE_DESCRIPTION, 0, 0, Lang.getMarkdownString(this, R.string.TypingInsteadInfo), false));

        items.add(new ListItem(ListItem.TYPE_HEADER, 0, 0, R.string.AutoPauseMedia));
        items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));
        items.add(new ListItem(ListItem.TYPE_VALUED_SETTING_COMPACT_WITH_TOGGLER, R.id.btn_autoPauseMedia, 0, R.string.AutoPauseMedia));
        items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));
        items.add(new ListItem(ListItem.TYPE_DESCRIPTION, 0, 0, R.string.AutoPauseMediaHint));
        break;
      case CATEGORY_MISC:
        items.add(new ListItem(ListItem.TYPE_HEADER, 0, 0, R.string.ExperimentalOptions));
        items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));
        items.add(new ListItem(ListItem.TYPE_VALUED_SETTING_COMPACT, R.id.btn_changeSizeLimit, 0, R.string.changeSizeLimit));
        items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));
        items.add(new ListItem(ListItem.TYPE_DESCRIPTION, 0, 0, Lang.getMarkdownString(this, R.string.changeSizeLimitInfo), false));
        items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));
        items.add(new ListItem(ListItem.TYPE_RADIO_SETTING, R.id.btn_enableFeaturesButton, 0, R.string.EnableFeatures));
        items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));
        break;
      default:
        items.add(new ListItem(ListItem.TYPE_HEADER, 0, 0, R.string.MoexAbout));
        items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));
        items.add(new ListItem(ListItem.TYPE_DESCRIPTION, 0, 0, Lang.getMarkdownString(this, R.string.MoexAboutText), false));
        items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));

        items.add(new ListItem(ListItem.TYPE_HEADER, 0, 0, R.string.MoexCategories));
        items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));
        items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_GeneralMoexSettings, R.drawable.baseline_settings_24, R.string.GeneralMoexSettings));
        items.add(new ListItem(ListItem.TYPE_SEPARATOR));
        items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_InterfaceMoexSettings, R.drawable.baseline_extension_24, R.string.InterfaceMoexSettings));
        items.add(new ListItem(ListItem.TYPE_SEPARATOR));
        items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_ChatsMoexSettings, R.drawable.baseline_chat_bubble_24, R.string.ChatsMoexSettings));
        items.add(new ListItem(ListItem.TYPE_SEPARATOR));
        items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_MiscMoexSettings, R.drawable.baseline_layers_24, R.string.Other));
        items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));

        items.add(new ListItem(ListItem.TYPE_HEADER, 0, 0, R.string.MoexLinks));
        items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));
        items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_moexCrowdinLink, R.drawable.baseline_translate_24, R.string.Translate));
        items.add(new ListItem(ListItem.TYPE_SEPARATOR));
        items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_moexChatLink, R.drawable.outline_forum_24, R.string.MoexChatText));
        items.add(new ListItem(ListItem.TYPE_SEPARATOR));
        items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_moexChannelLink, R.drawable.baseline_link_24, R.string.MoexChannelText));
        items.add(new ListItem(ListItem.TYPE_SEPARATOR));
        items.add(new ListItem(ListItem.TYPE_SETTING, R.id.btn_moexSourceLink, R.drawable.baseline_code_24, R.string.MoexSourceText));
        items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));

        items.add(new ListItem(ListItem.TYPE_BUILD_NO, R.id.btn_build, 0, R.string.MoexVer, false));
        break;
    }
    adapter.setItems(items, false);
    recyclerView.setAdapter(adapter);
  }
}