package moe.kirao.mgx.utils;

import androidx.annotation.NonNull;

import org.thunderdog.challegram.telegram.Tdlib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RecentChannelsTracker {
  public static final int MAX_ENTRIES = 5;

  private static final Map<Integer, LinkedHashMap<Long, Long>> RECENT_CHANNELS_HISTORY = new java.util.HashMap<>();

  private RecentChannelsTracker () {}

  public static void onChatOpened (@NonNull Tdlib tdlib, long chatId) {
    if (chatId == 0 || !tdlib.isChannel(chatId)) {
      return;
    }
    final int accountId = tdlib.id();
    synchronized (RECENT_CHANNELS_HISTORY) {
      LinkedHashMap<Long, Long> history = RECENT_CHANNELS_HISTORY.get(accountId);
      if (history == null) {
        history = new LinkedHashMap<>(MAX_ENTRIES + 1, 1f, false);
        RECENT_CHANNELS_HISTORY.put(accountId, history);
      }
      history.remove(chatId);
      history.put(chatId, System.currentTimeMillis());
      while (history.size() > MAX_ENTRIES) {
        history.remove(history.keySet().iterator().next());
      }
    }
  }

  @NonNull
  public static List<Long> getRecent (@NonNull Tdlib tdlib) {
    synchronized (RECENT_CHANNELS_HISTORY) {
      LinkedHashMap<Long, Long> history = RECENT_CHANNELS_HISTORY.get(tdlib.id());
      if (history == null || history.isEmpty()) {
        return Collections.emptyList();
      }
      List<Long> ordered = new ArrayList<>(history.keySet());
      Collections.reverse(ordered);
      return ordered;
    }
  }
}
