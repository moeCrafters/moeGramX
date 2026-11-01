/*
 * This file is created under moeGramX project development under GPLv3 license.
 * Copyright © Kira Roubin (jplie), 2024 (kirao@kiri.su)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 */
package moe.kirao.mgx.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.drinkless.tdlib.TdApi;
import org.jetbrains.annotations.NotNull;
import org.thunderdog.challegram.R;
import org.thunderdog.challegram.U;
import org.thunderdog.challegram.component.base.SettingView;
import org.thunderdog.challegram.core.Lang;
import org.thunderdog.challegram.data.TGMessage;
import org.thunderdog.challegram.data.ThreadInfo;
import org.thunderdog.challegram.navigation.HeaderView;
import org.thunderdog.challegram.telegram.Tdlib;
import org.thunderdog.challegram.telegram.TdlibUi;
import org.thunderdog.challegram.theme.ColorId;
import org.thunderdog.challegram.tool.Screen;
import org.thunderdog.challegram.tool.UI;
import org.thunderdog.challegram.ui.ListItem;
import org.thunderdog.challegram.ui.RecyclerViewController;
import org.thunderdog.challegram.ui.SettingsAdapter;
import org.thunderdog.challegram.ui.TextController;
import org.thunderdog.challegram.util.StringList;
import org.thunderdog.challegram.v.CustomRecyclerView;

import tgx.td.MessageId;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import me.vkryl.core.StringUtils;
import me.vkryl.core.collection.IntList;
import me.vkryl.core.lambda.RunnableData;
import moe.kirao.mgx.utils.ChatUtils;
import moe.kirao.mgx.utils.SystemUtils;

public class MessageDetailsController extends RecyclerViewController<MessageDetailsController.Args> implements View.OnClickListener {
  private static final Set<String> SKIPPED_FIELDS = Set.of(
    "outline", "data", "waveform", "minithumbnail", "id", "uniqueId", "remote"
  );

  public static final Gson gson = new GsonBuilder().addSerializationExclusionStrategy(new ExclusionStrategy() {
    @Override
    public boolean shouldSkipField (FieldAttributes f) {
      return SKIPPED_FIELDS.contains(f.getName());
    }

    @Override
    public boolean shouldSkipClass (Class<?> clazz) {
      return false;
    }
  }).setPrettyPrinting().create();

  public static class Args {
    public final TdApi.Message msg;
    public final ThreadInfo messageThread;

    public Args (TGMessage msg, ThreadInfo messageThread) {
      this.msg = msg.getMessage();
      this.messageThread = messageThread;
    }
  }

  private final Args args;
  private ContentInfo contentInfo;

  public MessageDetailsController (Context context, Tdlib tdlib, Args args) {
    super(context, tdlib);
    this.args = args;
  }

  @Override
  public int getId () {
    return R.id.controller_details;
  }

  @Override
  protected int getMenuId () {
    return R.id.menu_btn_view;
  }

  @Override
  public void fillMenuItems (int id, HeaderView header, LinearLayout menu) {
    if (id == R.id.menu_btn_view) {
      header.addButton(menu, R.id.menu_btn_view, R.drawable.baseline_text_search_variant_24, ColorId.headerIcon, this, Screen.dp(52f));
    }
  }

  @Override
  public void onMenuItemPressed (int id, View view) {
    if (id == R.id.menu_btn_view) {
      TextController c = new TextController(context, tdlib);
      c.setArguments(TextController.Arguments.fromRawText("JSON", gson.toJson(args.msg), "text/plain"));
      navigateTo(c);
    }
  }

  @Override
  public CharSequence getName () {
    return Lang.getString(R.string.MsgDetails);
  }

  private record ContentInfo(String text, String path, String formattedSize, String mime,
                             String name, String resolution, String duration, String bitrate,
                             String emoji, String packId, String storyId, String songName,
                             String performer, String platform, String dcName) {
  }

  @SuppressLint("SwitchIntDef")
  private ContentInfo buildContentInfo () {
    TdApi.Message msg = args.msg;
    String text = "", path = "", formattedSize = "", mime = "", name = "";
    String resolution = "", duration = "", bitrate = "", emoji = "", platform = "";
    String packId = "", storyId = "", songName = "", performer = "", dcName = "";

    switch (msg.content.getConstructor()) {
      case TdApi.MessageText.CONSTRUCTOR: {
        text = ((TdApi.MessageText) msg.content).text.text;
        break;
      }
      case TdApi.MessagePhoto.CONSTRUCTOR: {
        TdApi.MessagePhoto messagePhoto = (TdApi.MessagePhoto) msg.content;
        TdApi.PhotoSize[] sizes = messagePhoto.photo.sizes;
        TdApi.PhotoSize photoSize = sizes[sizes.length - 1];
        TdApi.File file = photoSize.photo;
        text = messagePhoto.caption.text;
        path = file.local.path;
        formattedSize = formatSize(file.expectedSize);
        mime = U.resolveMimeType(file.local.path);
        resolution = photoSize.width + "x" + photoSize.height;
        platform = SystemUtils.identifyFileHeader(path, 64);
        int dcId = SystemUtils.getDcIdFromRemoteId(sizes[0].photo.remote.id);
        dcName = dcId != 0 ? "DC" + dcId + ", " + ChatUtils.getDCName(dcId) : "";
        break;
      }
      case TdApi.MessageDocument.CONSTRUCTOR: {
        TdApi.MessageDocument messageDocument = (TdApi.MessageDocument) msg.content;
        TdApi.Document document = messageDocument.document;
        text = messageDocument.caption.text;
        path = document.document.local.path;
        formattedSize = formatSize(document.document.size);
        mime = document.mimeType;
        name = document.fileName;
        U.MediaMetadata meta = U.getMediaMetadata(document.document.local.path);
        resolution = meta != null ? meta.width + "x" + meta.height : getDocumentRes(document.document.local.path);
        break;
      }
      case TdApi.MessageVideo.CONSTRUCTOR: {
        TdApi.MessageVideo messageVideo = (TdApi.MessageVideo) msg.content;
        TdApi.Video video = messageVideo.video;
        text = messageVideo.caption.text;
        path = video.video.local.path;
        formattedSize = formatSize(video.video.expectedSize);
        mime = video.mimeType;
        name = video.fileName;
        resolution = video.width + "x" + video.height;
        duration = video.duration != 0 ? DateUtils.formatElapsedTime(video.duration) : "";
        U.MediaMetadata meta = U.getMediaMetadata(video.video.local.path);
        bitrate = (meta != null ? meta.bitrate / 1000 : video.video.expectedSize / video.duration * 8 / 1000) + " Kbps";
        break;
      }
      case TdApi.MessageSticker.CONSTRUCTOR: {
        TdApi.Sticker sticker = ((TdApi.MessageSticker) msg.content).sticker;
        path = sticker.sticker.local.path;
        formattedSize = formatSize(sticker.sticker.expectedSize);
        mime = U.resolveMimeType(sticker.sticker.local.path);
        resolution = sticker.width + "x" + sticker.height;
        emoji = sticker.emoji;
        packId = String.valueOf(sticker.setId);
        break;
      }
      case TdApi.MessageAudio.CONSTRUCTOR: {
        TdApi.MessageAudio messageAudio = (TdApi.MessageAudio) msg.content;
        TdApi.Audio audio = messageAudio.audio;
        U.MediaMetadata meta = U.getMediaMetadata(audio.audio.local.path);
        text = messageAudio.caption.text;
        path = audio.audio.local.path;
        formattedSize = formatSize(audio.audio.expectedSize);
        mime = audio.mimeType;
        name = audio.fileName;
        songName = meta != null ? meta.title : audio.title;
        performer = meta != null ? meta.performer : audio.performer;
        duration = audio.duration != 0 ? DateUtils.formatElapsedTime(audio.duration) : "";
        bitrate = meta != null ? meta.bitrate / 1000 + " Kbps" : audio.duration != 0 ? audio.audio.expectedSize / audio.duration * 8 / 1000 + " Kbps" : "";
        break;
      }
      case TdApi.MessageAnimation.CONSTRUCTOR: {
        TdApi.MessageAnimation messageAnimation = (TdApi.MessageAnimation) msg.content;
        TdApi.Animation animation = messageAnimation.animation;
        text = messageAnimation.caption.text;
        path = animation.animation.local.path;
        formattedSize = formatSize(animation.animation.expectedSize);
        mime = animation.mimeType;
        name = animation.fileName;
        duration = DateUtils.formatElapsedTime(animation.duration);
        resolution = animation.width + "x" + animation.height;
        break;
      }
      case TdApi.MessageVoiceNote.CONSTRUCTOR: {
        TdApi.MessageVoiceNote messageVoiceNote = (TdApi.MessageVoiceNote) msg.content;
        TdApi.VoiceNote voiceNote = messageVoiceNote.voiceNote;
        text = messageVoiceNote.caption.text;
        path = voiceNote.voice.local.path;
        formattedSize = formatSize(voiceNote.voice.expectedSize);
        mime = voiceNote.mimeType;
        duration = DateUtils.formatElapsedTime(voiceNote.duration);
        U.MediaMetadata meta = U.getMediaMetadata(voiceNote.voice.local.path);
        bitrate = (meta != null ? meta.bitrate / 1000 : voiceNote.voice.expectedSize / voiceNote.duration * 8 / 1000) + " Kbps";
        break;
      }
      case TdApi.MessageVideoNote.CONSTRUCTOR: {
        TdApi.VideoNote videoNote = ((TdApi.MessageVideoNote) msg.content).videoNote;
        U.MediaMetadata meta = U.getMediaMetadata(videoNote.video.local.path);
        path = videoNote.video.local.path;
        formattedSize = formatSize(videoNote.video.expectedSize);
        mime = U.resolveMimeType(videoNote.video.local.path);
        duration = DateUtils.formatElapsedTime(videoNote.duration);
        resolution = meta != null ? meta.width + "x" + meta.height : "";
        bitrate = (meta != null ? meta.bitrate / 1000 : videoNote.video.expectedSize / videoNote.duration * 8 / 1000) + " Kbps";
        break;
      }
      case TdApi.MessageAnimatedEmoji.CONSTRUCTOR: {
        TdApi.AnimatedEmoji animatedEmoji = ((TdApi.MessageAnimatedEmoji) msg.content).animatedEmoji;
        if (animatedEmoji.sticker != null) {
          path = animatedEmoji.sticker.sticker.local.path;
          formattedSize = formatSize(animatedEmoji.sticker.sticker.expectedSize);
          mime = U.resolveMimeType(animatedEmoji.sticker.sticker.local.path);
          resolution = animatedEmoji.sticker.width + "x" + animatedEmoji.sticker.height;
          emoji = animatedEmoji.sticker.emoji;
          packId = String.valueOf(animatedEmoji.sticker.setId);
        }
        break;
      }
      case TdApi.MessageStory.CONSTRUCTOR: {
        TdApi.MessageStory story = (TdApi.MessageStory) msg.content;
        storyId = String.valueOf(story.storyId);
        break;
      }
    }

    return new ContentInfo(text, path, formattedSize, mime, name, resolution, duration, bitrate, emoji, packId, storyId, songName, performer, platform, dcName);
  }

  private static String formatSize (long size) {
    return Formatter.formatShortFileSize(UI.getContext(), size);
  }

  private String getDate (int unixt, boolean Edited) {
    return Edited ? Lang.getModifiedTimestamp(unixt, TimeUnit.SECONDS) : Lang.getRelativeTimestamp(unixt, TimeUnit.SECONDS);
  }

  private void openPath (@NotNull String path) {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    intent.putExtra(Intent.EXTRA_STREAM, SystemUtils.getUri(path));
    intent.setDataAndType(SystemUtils.getUri(path), U.resolveMimeType(path));
    context.startActivity(Intent.createChooser(intent, null));
  }

  private @NotNull String getDocumentRes (@NotNull String path) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeFile(path, options);
    return options.outWidth + "x" + options.outHeight;
  }

  private int getConstructor () {
    return args.msg.content.getConstructor();
  }

  private long getAuthorId () {
    return switch (getConstructor()) {
      case TdApi.MessageSticker.CONSTRUCTOR -> {
        TdApi.Sticker sticker = ((TdApi.MessageSticker) args.msg.content).sticker;
        yield ChatUtils.extractAuthorId(sticker.setId);
      }
      case TdApi.MessageAnimatedEmoji.CONSTRUCTOR -> {
        TdApi.Sticker animatedEmoji = ((TdApi.MessageAnimatedEmoji) args.msg.content).animatedEmoji.sticker;
        yield animatedEmoji != null ? ChatUtils.extractAuthorId(animatedEmoji.setId) : 0;
      }
      case TdApi.MessageStory.CONSTRUCTOR -> {
        TdApi.MessageStory story = ((TdApi.MessageStory) args.msg.content);
        yield story.storyPosterChatId;
      }
      default -> 0;
    };
  }

  private boolean hasEdited () {
    return args.msg.editDate != 0 && args.msg.editDate != args.msg.date;
  }

  private boolean hasCaption () {
    return !StringUtils.isEmpty(contentInfo.text) && getConstructor() != TdApi.MessageText.CONSTRUCTOR;
  }

  private boolean hasValidResolution () {
    String res = contentInfo.resolution;
    return !StringUtils.isEmpty(res) && !res.equals("0x0") && !res.equals("-1x-1");
  }

  private boolean hasAuthor () {
    return getAuthorId() != 0 && (
      (getConstructor() == TdApi.MessageSticker.CONSTRUCTOR && !contentInfo.packId.equals("0")) ||
        getConstructor() == TdApi.MessageAnimatedEmoji.CONSTRUCTOR ||
        getConstructor() == TdApi.MessageStory.CONSTRUCTOR
    );
  }

  private void fetchAuthor (@NotNull RunnableData<ChatUtils.AuthorInfo> after) {
    ChatUtils.AuthorInfo localInfo = ChatUtils.resolveUserLocal(tdlib, getAuthorId());
    if (localInfo != null) {
      after.runWithData(localInfo);
    } else {
      ChatUtils.processAuthorRequest(tdlib, getAuthorId(), after);
    }
  }

  private boolean isAuthorLocal () {
    return ChatUtils.resolveUserLocal(tdlib, getAuthorId()) != null;
  }

  private void openActions (@NotNull IntList ids, @NotNull StringList strings, @NotNull IntList icons, int buttonId, @StringRes int buttonStringId, int buttonIconId, @NotNull CharSequence info, @NotNull CharSequence text, @Nullable String data) {
    ids.append(R.id.btn_copyText);
    strings.append(R.string.Copy);
    icons.append(R.drawable.baseline_content_copy_24);

    if (buttonId != 0 && !(buttonId == R.id.btn_inlineOpen && StringUtils.isEmptyOrBlank(data))) {
      ids.append(buttonId);
      strings.append(buttonStringId);
      icons.append(buttonIconId);
    }

    showOptions(info, ids.get(), strings.get(), null, icons.get(), (itemView, id) -> {
      if (id == R.id.btn_copyText) {
        UI.copyText(text, R.string.CopiedText);
      } else if (id == R.id.btn_openGroupProfile && data != null) {
        tdlib.ui().openChatProfile(this, getConstructor() == TdApi.MessageStory.CONSTRUCTOR || isAuthorLocal() ?
          Long.parseLong(data) : args.msg.chatId, args.messageThread, new TdlibUi.UrlOpenParameters().tooltip(context().tooltipManager().builder(itemView)));
      } else if (id == R.id.btn_openProfile) {
        tdlib.ui().openSenderProfile(this, args.msg.senderId, new TdlibUi.UrlOpenParameters().tooltip(context().tooltipManager().builder(itemView)));
      } else if (id == R.id.btn_openPath && data != null) {
        openPath(data);
      } else if (id == R.id.btn_inlineOpen && data != null) {
        tdlib.ui().openUrl(this, tdlib.tMeUrl(data), null, null);
      }
      return true;
    });
  }

  private void showCopyAction (String value) {
    IntList ids = new IntList(1);
    StringList strings = new StringList(1);
    IntList icons = new IntList(1);
    openActions(ids, strings, icons, 0, 0, 0, value, value, null);
  }

  private @StringRes int getContentHeaderRes () {
    return switch (getConstructor()) {
      case TdApi.MessageSticker.CONSTRUCTOR -> R.string.Sticker;
      case TdApi.MessagePhoto.CONSTRUCTOR -> R.string.Photo;
      case TdApi.MessageDocument.CONSTRUCTOR -> R.string.Document;
      case TdApi.MessageVideoNote.CONSTRUCTOR, TdApi.MessageVideo.CONSTRUCTOR -> R.string.Video;
      case TdApi.MessageAudio.CONSTRUCTOR -> R.string.Audio;
      case TdApi.MessageAnimation.CONSTRUCTOR -> R.string.Gif;
      case TdApi.MessageVoiceNote.CONSTRUCTOR -> R.string.Voice;
      case TdApi.MessageAnimatedEmoji.CONSTRUCTOR -> R.string.AnimatedEmoji;
      case TdApi.MessageStory.CONSTRUCTOR -> R.string.RightStories;
      default -> R.string.Message;
    };
  }


  @Override
  public void onClick (@NotNull View v) {
    int viewId = v.getId();

    if (viewId == R.id.btn_chatIdDetails) {
      String username = !StringUtils.isEmpty(tdlib.chatUsername(args.msg.chatId)) ? '@' + tdlib.chatUsername(args.msg.chatId) : "";
      String title = tdlib.chatTitle(args.msg.chatId);
      String chatId = String.valueOf(args.msg.chatId);
      String senderInfo = tdlib.isSelfChat(args.msg.chatId) ? title : String.join("\n", chatId, title, username).trim();
      IntList ids = new IntList(3);
      StringList strings = new StringList(3);
      IntList icons = new IntList(3);
      openActions(ids, strings, icons, R.id.btn_openGroupProfile, R.string.Open, R.drawable.baseline_group_24, senderInfo, senderInfo, chatId);
    } else if (viewId == R.id.btn_chatId) {
      showCopyAction(String.valueOf(MessageId.toServerMessageId(args.msg.id)));
    } else if (viewId == R.id.btn_dateDetails) {
      String date = hasEdited() ?
        String.join("\n", Lang.getString(R.string.Sent) + " " + getDate(args.msg.date, false), Lang.getString(R.string.Edited) + " " + getDate(args.msg.editDate, false))
        : getDate(args.msg.date, false);
      showCopyAction(date);
    } else if (viewId == R.id.btn_senderDetails) {
      TdApi.MessageSender sender = args.msg.senderId;
      String username = !StringUtils.isEmpty(tdlib.senderUsername(sender)) ? '@' + tdlib.senderUsername(sender) : "";
      String signature = args.msg.authorSignature;
      boolean hasSignature = !StringUtils.isEmptyOrBlank(signature);
      String name = hasSignature ? signature : tdlib.senderName(sender);
      String userId = String.valueOf(tdlib.isChannel(sender) ? args.msg.chatId : tdlib.senderUserId(args.msg));
      String senderInfo = hasSignature ? signature : String.join("\n", userId, name, username).trim();
      IntList ids = new IntList(3);
      StringList strings = new StringList(3);
      IntList icons = new IntList(3);
      openActions(ids, strings, icons, R.id.btn_openProfile, R.string.Open, R.drawable.dot_baseline_acc_personal_24, senderInfo, senderInfo, userId);
    } else if (viewId == R.id.btn_authorDetails) {
      fetchAuthor(info -> {
        String displayText = info.displayText();
        String username = info.username();

        RunnableData<Boolean> showAuthorActions = canOpen -> {
          IntList ids = new IntList(3);
          StringList strings = new StringList(3);
          IntList icons = new IntList(3);
          if (canOpen) {
            boolean openById = isAuthorLocal() && getConstructor() != TdApi.MessageStory.CONSTRUCTOR;
            String openData = openById ? String.valueOf(info.id()) : username;
            openActions(ids, strings, icons, openById ?
              R.id.btn_openGroupProfile : R.id.btn_inlineOpen, R.string.Open, R.drawable.dot_baseline_acc_personal_24, displayText, displayText, openData);
          } else {
            openActions(ids, strings, icons, 0, 0, 0, displayText, displayText, null);
          }
        };

        if (username != null) {
          tdlib.client().send(new TdApi.SearchPublicChat(username), result ->
            runOnUiThreadOptional(() -> showAuthorActions.runWithData(result.getConstructor() == TdApi.Chat.CONSTRUCTOR))
          );
        } else {
          showAuthorActions.runWithData(false);
        }
      });
    } else if (viewId == R.id.btn_filePath) {
      String path = contentInfo.path;
      IntList ids = new IntList(3);
      StringList strings = new StringList(3);
      IntList icons = new IntList(3);
      openActions(ids, strings, icons, R.id.btn_openPath, R.string.Open, R.drawable.baseline_sd_storage_24, path, path, path);
    } else if (viewId == R.id.btn_size) {
      showCopyAction(contentInfo.formattedSize);
    } else if (viewId == R.id.btn_mime) {
      showCopyAction(contentInfo.mime);
    } else if (viewId == R.id.btn_fileRes) {
      showCopyAction(contentInfo.resolution);
    } else if (viewId == R.id.btn_fileCaption) {
      showCopyAction(contentInfo.text);
    } else if (viewId == R.id.btn_fileName) {
      showCopyAction(contentInfo.name);
    } else if (viewId == R.id.btn_fileDuration) {
      showCopyAction(contentInfo.duration);
    } else if (viewId == R.id.btn_audioPerformerDetails) {
      showCopyAction(contentInfo.performer);
    } else if (viewId == R.id.btn_audioSongNameDetails) {
      showCopyAction(contentInfo.songName);
    } else if (viewId == R.id.btn_stickerEmojiDetails) {
      showCopyAction(contentInfo.emoji);
    } else if (viewId == R.id.btn_mediaBitrate) {
      showCopyAction(contentInfo.bitrate);
    } else if (viewId == R.id.btn_storyId) {
      showCopyAction(contentInfo.storyId);
    } else if (viewId == R.id.btn_sessionPlatform) {
      showCopyAction(contentInfo.platform);
    } else if (viewId == R.id.btn_peer_id) {
      showCopyAction(contentInfo.dcName);
    }
  }

  @Override
  protected void onCreateView (@NotNull Context context, @NotNull CustomRecyclerView recyclerView) {
    contentInfo = buildContentInfo();

    SettingsAdapter adapter = new SettingsAdapter(this) {
      @Override
      protected void setValuedSetting (@NotNull ListItem item, @NotNull SettingView view, boolean isUpdate) {
        view.setDrawModifier(item.getDrawModifier());
        int itemId = item.getId();
        if (itemId == R.id.btn_chatIdDetails) {
          view.setData(tdlib.chatTitle(args.msg.chatId));
        } else if (itemId == R.id.btn_chatId) {
          view.setData(String.valueOf(MessageId.toServerMessageId(args.msg.id)));
        } else if (itemId == R.id.btn_dateDetails) {
          view.setData(hasEdited() ? getDate(args.msg.editDate, true) : getDate(args.msg.date, false));
        } else if (itemId == R.id.btn_fileCaption) {
          view.setData(contentInfo.text);
        } else if (itemId == R.id.btn_senderDetails) {
          TdApi.MessageSender sender = args.msg.senderId;
          boolean hasSignature = !StringUtils.isEmptyOrBlank(args.msg.authorSignature);
          view.setData(tdlib.isChannel(sender) ?
            hasSignature ? args.msg.authorSignature : tdlib.senderName(sender) :
            tdlib.senderName(sender));
        } else if (itemId == R.id.btn_authorDetails) {
          view.setData(Lang.getString(R.string.LoadingInformation));
          fetchAuthor(info -> view.setData(info != null && info.name() != null ? info.name() : Lang.getString(R.string.PhoneNumberUnknown)));
        } else if (itemId == R.id.btn_filePath) {
          view.setData(R.string.Open);
        } else if (itemId == R.id.btn_size) {
          view.setData(contentInfo.formattedSize);
        } else if (itemId == R.id.btn_mime) {
          view.setData(contentInfo.mime);
        } else if (itemId == R.id.btn_fileRes) {
          view.setData(contentInfo.resolution);
        } else if (itemId == R.id.btn_fileName) {
          view.setData(contentInfo.name);
        } else if (itemId == R.id.btn_fileDuration) {
          view.setData(contentInfo.duration);
        } else if (itemId == R.id.btn_audioPerformerDetails) {
          view.setData(contentInfo.performer);
        } else if (itemId == R.id.btn_audioSongNameDetails) {
          view.setData(contentInfo.songName);
        } else if (itemId == R.id.btn_stickerEmojiDetails) {
          view.setData(contentInfo.emoji);
        } else if (itemId == R.id.btn_mediaBitrate) {
          view.setData(contentInfo.bitrate);
        } else if (itemId == R.id.btn_storyId) {
          view.setData(contentInfo.storyId);
        } else if (itemId == R.id.btn_sessionPlatform) {
          view.setData(contentInfo.platform);
        } else if (itemId == R.id.btn_peer_id) {
          view.setData(contentInfo.dcName);
        }
      }
    };

    ArrayList<ListItem> items = new ArrayList<>();
    items.add(new ListItem(ListItem.TYPE_EMPTY_OFFSET_SMALL));
    items.add(new ListItem(ListItem.TYPE_HEADER, 0, 0, getContentHeaderRes()));
    items.add(new ListItem(ListItem.TYPE_SHADOW_TOP));

    boolean isUserChat = !tdlib.isChannel(args.msg.senderId) && tdlib.senderUserId(args.msg) == args.msg.chatId;
    if (!isUserChat || tdlib.isSelfChat(args.msg.chatId)) {
      items.add(new ListItem(ListItem.TYPE_INFO_SETTING, R.id.btn_chatIdDetails, R.drawable.baseline_chat_bubble_24, tdlib.isChannel(args.msg.senderId) ? R.string.Channel : tdlib.isUserChat(args.msg.chatId) ? R.string.Chat : R.string.Group));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    }
    items.add(new ListItem(ListItem.TYPE_INFO_SETTING, R.id.btn_chatId, R.drawable.baseline_identifier_24, R.string.Message));
    items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    items.add(new ListItem(ListItem.TYPE_INFO_SETTING, R.id.btn_dateDetails, R.drawable.baseline_date_range_24, hasEdited() ? Lang.getString(R.string.Date) + '*' : Lang.getString(R.string.Date)));
    items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    boolean hasSignature = !StringUtils.isEmptyOrBlank(args.msg.authorSignature);
    if (!tdlib.isChannel(args.msg.senderId) || hasSignature) {
      items.add(new ListItem(ListItem.TYPE_INFO_SETTING, R.id.btn_senderDetails, R.drawable.baseline_person_24, R.string.SenderId));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    }
    if (!StringUtils.isEmptyOrBlank(contentInfo.mime)) {
      items.add(new ListItem(ListItem.TYPE_INFO_SETTING, R.id.btn_mime, R.drawable.baseline_extension_24, R.string.MimeType));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    }
    if (hasValidResolution()) {
      items.add(new ListItem(ListItem.TYPE_INFO_SETTING, R.id.btn_fileRes, R.drawable.baseline_crop_original_24, R.string.FileRes));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    }
    if (!StringUtils.isEmptyOrBlank(contentInfo.platform)) {
      items.add(new ListItem(ListItem.TYPE_INFO_SETTING, R.id.btn_sessionPlatform, R.drawable.baseline_device_other_24, R.string.Platform));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    }
    if (!StringUtils.isEmptyOrBlank(contentInfo.dcName)) {
      items.add(new ListItem(ListItem.TYPE_INFO_SETTING, R.id.btn_peer_id, R.drawable.baseline_language_24, R.string.DcID));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    }
    if (getConstructor() == TdApi.MessageText.CONSTRUCTOR || hasCaption()) {
      items.add(new ListItem(ListItem.TYPE_INFO_SETTING, R.id.btn_fileCaption, R.drawable.baseline_format_text_24, R.string.Message));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    }
    if (!StringUtils.isEmptyOrBlank(contentInfo.path)) {
      items.add(new ListItem(ListItem.TYPE_INFO_SETTING, R.id.btn_filePath, R.drawable.baseline_map_24, R.string.FilePath));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    }
    if (!StringUtils.isEmptyOrBlank(contentInfo.formattedSize)) {
      items.add(new ListItem(ListItem.TYPE_INFO_SETTING, R.id.btn_size, R.drawable.baseline_sd_storage_24, R.string.FileSize));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    }
    if (!StringUtils.isEmptyOrBlank(contentInfo.name)) {
      items.add(new ListItem(ListItem.TYPE_INFO_SETTING, R.id.btn_fileName, R.drawable.deproko_baseline_text_add_24, R.string.FileName));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    }
    if (!StringUtils.isEmptyOrBlank(contentInfo.duration)) {
      items.add(new ListItem(ListItem.TYPE_INFO_SETTING, R.id.btn_fileDuration, R.drawable.baseline_access_time_24, R.string.Duration));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    }
    if (!StringUtils.isEmptyOrBlank(contentInfo.bitrate)) {
      items.add(new ListItem(ListItem.TYPE_INFO_SETTING, R.id.btn_mediaBitrate, R.drawable.baseline_bar_chart_24, R.string.Bitrate));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    }
    if (!StringUtils.isEmptyOrBlank(contentInfo.performer)) {
      items.add(new ListItem(ListItem.TYPE_INFO_SETTING, R.id.btn_audioPerformerDetails, R.drawable.baseline_person_24, R.string.FilePerformer));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    }
    if (!StringUtils.isEmptyOrBlank(contentInfo.songName)) {
      items.add(new ListItem(ListItem.TYPE_INFO_SETTING, R.id.btn_audioSongNameDetails, R.drawable.baseline_music_note_24, R.string.FileSongName));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    }
    if (hasAuthor()) {
      items.add(new ListItem(ListItem.TYPE_INFO_SETTING, R.id.btn_authorDetails, R.drawable.baseline_info_24, R.string.SetId));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    }
    if (!StringUtils.isEmptyOrBlank(contentInfo.storyId)) {
      items.add(new ListItem(ListItem.TYPE_INFO_SETTING, R.id.btn_storyId, R.drawable.baseline_book_24, R.string.StoryId));
      items.add(new ListItem(ListItem.TYPE_SEPARATOR_FULL));
    }
    if (!StringUtils.isEmptyOrBlank(contentInfo.emoji)) {
      items.add(new ListItem(ListItem.TYPE_INFO_SETTING, R.id.btn_stickerEmojiDetails, R.drawable.baseline_emoticon_24, R.string.EmojiHeader));
    }
    items.add(new ListItem(ListItem.TYPE_SHADOW_BOTTOM));

    adapter.setItems(items, false);
    recyclerView.setAdapter(adapter);
  }
}