package moe.kirao.mgx.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.drinkless.tdlib.TdApi;
import org.thunderdog.challegram.telegram.Tdlib;
import org.thunderdog.challegram.tool.UI;

import me.vkryl.core.StringUtils;
import me.vkryl.core.lambda.RunnableData;

public class ChatUtils {
  private static final long SCRAPER_BOT_ID = 7424190611L;

  public record AuthorInfo(long id, @Nullable String name, @Nullable String username) {
    public @NonNull String displayText () {
      if (name == null) return String.valueOf(id);
      if (username != null) {
        return String.join("\n", String.valueOf(id), name, '@' + username);
      }
      return String.join("\n", String.valueOf(id), name);
    }
  }

  @Nullable
  public static AuthorInfo resolveUserLocal (@NonNull Tdlib tdlib, long userId) {
    TdApi.MessageSender sender = tdlib.sender(userId);
    if (tdlib.senderName(sender).startsWith("User#")) return null;
    String username = tdlib.senderUsername(sender);
    return new AuthorInfo(userId, tdlib.senderName(sender), StringUtils.isEmptyOrBlank(username) ? null : username);
  }

  public static void processAuthorRequest (@NonNull Tdlib tdlib, long authorId, @NonNull RunnableData<AuthorInfo> after) {
    Runnable request = () ->
      tdlib.client().send(new TdApi.GetInlineQueryResults(SCRAPER_BOT_ID, tdlib.selfChatId(), null, String.valueOf(authorId), null), object -> {
        if (object.getConstructor() != TdApi.InlineQueryResults.CONSTRUCTOR) return;
        TdApi.InlineQueryResults results = (TdApi.InlineQueryResults) object;
        if (results.results.length == 0) {
          UI.post(() -> after.runWithData(new AuthorInfo(authorId, null, null)));
          return;
        }
        String resultId = ((TdApi.InlineQueryResultArticle) results.results[0]).id;
        tdlib.client().send(new TdApi.SendInlineQueryResultMessage(tdlib.selfChatId(), null, null, new TdApi.MessageSendOptions(null, false, false, false, false, 0, false, null, 0, 0, true), results.inlineQueryId, resultId, false), newMsg -> {
          if (newMsg.getConstructor() != TdApi.Message.CONSTRUCTOR) return;
          String text = ((TdApi.MessageText) ((TdApi.Message) newMsg).content).text.text;
          AuthorInfo parsed = parseAuthorResponse(text);
          UI.post(() -> after.runWithData(parsed != null ? parsed : new AuthorInfo(authorId, null, null)));
        });
      });
    if (tdlib.senderName(tdlib.sender(SCRAPER_BOT_ID)).startsWith("User#")) {
      tdlib.client().send(new TdApi.SearchPublicChat("tgdb_search_bot"), chat -> request.run());
    } else {
      request.run();
    }
  }

  private static AuthorInfo parseAuthorResponse (@NonNull String text) {
    long id = 0;
    String name = null;
    String username = null;
    for (String line : text.split("\n")) {
      if (line.startsWith("\uD83C\uDD94")) {
        id = Long.parseLong(line.replaceAll("^.+?: ?", ""));
      } else if (line.startsWith("\uD83C\uDFF7")) {
        name = line.replaceAll("^.+?: ?", "");
      } else if (line.startsWith("\uD83D\uDCE7")) {
        username = line.replaceAll("^.+?: @?", "");
      }
    }
    if (id == 0) return null;
    return new AuthorInfo(id, name, username);
  }

  public static long extractAuthorId (long setId) {
    long authorId = setId >> 32;

    if (((setId >> 16) & 0xff) == 0x3f) {
      authorId |= 0x800000000L;
    }
    if (((setId >> 24) & 0xff) != 0) {
      authorId += 0x100000000L;
    }
    return authorId;
  }

  public static String getDCName (int dc) {
    return switch (dc) {
      case 1, 3 -> "Miami FL, USA";
      case 2, 4 -> "Amsterdam, NL";
      case 5 -> "Singapore, SG";
      default -> null;
    };
  }
}