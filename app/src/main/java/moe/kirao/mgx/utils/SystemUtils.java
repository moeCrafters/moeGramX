package moe.kirao.mgx.utils;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.util.Pair;

import org.drinkless.tdlib.TdApi;
import org.thunderdog.challegram.BaseActivity;
import org.thunderdog.challegram.FileProvider;
import org.thunderdog.challegram.Log;
import org.thunderdog.challegram.R;
import org.thunderdog.challegram.U;
import org.thunderdog.challegram.config.Config;
import org.thunderdog.challegram.core.Background;
import org.thunderdog.challegram.core.Lang;
import org.thunderdog.challegram.data.TD;
import org.thunderdog.challegram.tool.UI;
import org.thunderdog.challegram.unsorted.AppContext;
import org.thunderdog.challegram.util.Permissions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.vkryl.core.StringUtils;

public class SystemUtils {
  static ArrayList<Pair<String, String>> headers = new ArrayList<>() {{
    add(new Pair<>("FFD8FFE000104A46494600010101006000600000FFDB0043000403030403030404030405040405060A07060606060D090A080A0F0D10100F0D0F0E1113181411", "Desktop"));
    add(new Pair<>("FFD8FFE000104A46494600010101004800480000FFE202184943435F50524F46494C4500010100000208", "Web, K"));
    add(new Pair<>("FFD8FFE000104A46494600010100000100010000FFE202184943435F50524F46494C450001010000020800000000043000006D6E74725247422058595A2007E0", "Android"));
    add(new Pair<>("FFD8FFE000104A46494600010101004800480000FFE201D84943435F50524F46494C45000101000001C800000000043000006D6E74725247422058595A2007E0", "Android"));
    add(new Pair<>("FFD8FFE000104A46494600010100000100010000FFE201D84943435F50524F46494C45000101000001C800000000043000006D6E74725247422058595A2007E0", "Android"));
    add(new Pair<>("FFD8FFE000104A46494600010100000100010000FFDB004300090607080706090807080A0A090B0D160F0D0C0C0D1B14151016201D2222201D1F1F2428342C24", "iOS"));
    add(new Pair<>("FFD8FFE000104A46494600010100000100010000FFDB004300080606070605080707070909080A0C140D0C0B0B0C1912130F141D1A1F1E1D1A1C1C20242E2720", "macOS"));
    add(new Pair<>("FFD8FFE000104A46494600010101004800480000FFE201DB4943435F50524F46494C45000101000001CB00000000024000006D6E74725247422058595A200000", "macOS"));
  }};

  public static boolean shouldShowClipboardToast () {
    return ((Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) || OEMUtils.isMIUI()) && ((Build.VERSION.SDK_INT < Build.VERSION_CODES.S) || !OEMUtils.hasBuiltInClipboardToasts());
  }

  public static void copyFileToClipboard (TdApi.File file, @StringRes int toast) {
    try {
      ClipboardManager clipboard = (ClipboardManager) AppContext.get().getSystemService(Context.CLIPBOARD_SERVICE);
      if (clipboard != null) {
        ClipData clip = ClipData.newUri(AppContext.get().getContentResolver(), "image", getUri(file.local.path));
        clipboard.setPrimaryClip(clip);
        if (shouldShowClipboardToast()) {
          UI.showToast(toast, Toast.LENGTH_SHORT);
        }
      }
    } catch (Exception e) {
      Log.e(e);
    }
  }

  public static void saveFileToGallery (BaseActivity context, List<TD.DownloadedFile> files) {
    try {
      if (context.permissions().requestWriteExternalStorage(Permissions.WriteType.GALLERY, granted -> {
        if (granted) {
          saveFileToGallery(context, files);
        }
      })) {
        return;
      }
      Background.instance().post(() -> {
        int savedCount = 0;
        for (TD.DownloadedFile file : files) {
          if (file.getMimeType().startsWith("image/")) {
            if (U.copyToGalleryImpl(file.getPath(), U.TYPE_PHOTO, null)) {
              savedCount++;
            }
          } else if (file.getMimeType().startsWith("video/")) {
            if (U.copyToGalleryImpl(file.getPath(), U.TYPE_VIDEO, null)) {
              savedCount++;
            }
          }
        }
        if (savedCount > 0) {
          if (savedCount == 1) {
            String mime = files.get(0).getMimeType();
            if (mime.startsWith("image/")) {
              UI.showToast(R.string.PhotoHasBeenSavedToGallery, Toast.LENGTH_SHORT);
            } else if (mime.startsWith("video/")) {
              UI.showToast(R.string.VideoHasBeenSavedToGallery, Toast.LENGTH_SHORT);
            } else {
              UI.showToast(R.string.GifHasBeenSavedToGallery, Toast.LENGTH_SHORT);
            }
          } else {
            UI.showToast(Lang.pluralBold(R.string.SavedXFiles, savedCount), Toast.LENGTH_SHORT);
          }
        }
      });
    } catch (Exception e) {
      Log.e(e);
    }
  }

  public static void saveFileToGallery (BaseActivity context, String path) {
    try {
      if (context.permissions().requestWriteExternalStorage(Permissions.WriteType.GALLERY, granted -> {
        if (granted) {
          saveFileToGallery(context, path);
        }
      })) {
        return;
      }
      Background.instance().post(() -> {
        String mime = U.resolveMimeType(path);
        if (mime.startsWith("image/")) {
          if (U.copyToGalleryImpl(path, U.TYPE_PHOTO, null)) {
            UI.showToast(R.string.PhotoHasBeenSavedToGallery, Toast.LENGTH_SHORT);
          }
        } else if (mime.startsWith("video/")) {
          if (U.copyToGalleryImpl(path, U.TYPE_VIDEO, null)) {
            UI.showToast(R.string.VideoHasBeenSavedToGallery, Toast.LENGTH_SHORT);
          }
        }
      });
    } catch (Exception e) {
      Log.e(e);
    }
  }

  public static Uri getUri (String path) {
    return FileProvider.getUriForFile(AppContext.get(), Config.FILE_PROVIDER_AUTHORITY, new File(path));
  }

  @Nullable
  public static String identifyFileHeader (String filePath, int headerSize) {
    byte[] byteArray;
    try {
      byteArray = extractFileHeader(filePath, headerSize);
      String hex = U.bytesToHex(byteArray);
      for (Pair<String, String> pair : headers) {
        if (hex.startsWith(pair.first))
          return pair.second;
      }
    } catch (IOException ignore) {}
    return null;
  }

  private static byte[] extractFileHeader (String path, int endByte) throws IOException {
    File file = new File(path);
    byte[] buffer = new byte[endByte];
    try (FileInputStream inputStream = new FileInputStream(file)) {
      int bytesRead = inputStream.read(buffer);
      if (bytesRead < 1) return null;
    }
    return buffer;
  }

  public static int getDcIdFromRemoteId (@Nullable String remoteId) {
    if (StringUtils.isEmptyOrBlank(remoteId)) {
      return 0;
    }
    try {
      byte[] data = rleDecode(Base64.decode(remoteId, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP));
      if (data.length < 8) {
        return 0;
      }
      int dcId = (data[4] & 0xFF) | ((data[5] & 0xFF) << 8) | ((data[6] & 0xFF) << 16) | ((data[7] & 0xFF) << 24);
      return (dcId >= 1 && dcId <= 5) ? dcId : 0;
    } catch (Exception ignored) {
      return 0;
    }
  }

  private static byte[] rleDecode (byte[] input) {
    ByteArrayOutputStream out = new ByteArrayOutputStream(input.length);
    for (int i = 0; i < input.length; i++) {
      if (input[i] == 0 && i + 1 < input.length) {
        int count = input[++i] & 0xFF;
        for (int j = 0; j < count; j++) {
          out.write(0);
        }
      } else {
        out.write(input[i]);
      }
    }
    return out.toByteArray();
  }

  public static boolean isVpnActive () {
    try {
      ConnectivityManager cm = (ConnectivityManager) AppContext.get().getSystemService(Context.CONNECTIVITY_SERVICE);
      if (cm == null) {
        return false;
      }
      Network network = cm.getActiveNetwork();
      if (network == null) {
        return false;
      }
      NetworkCapabilities caps = cm.getNetworkCapabilities(network);
      return caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
    } catch (Throwable t) {
      Log.e("Unable to detect VPN", t);
    }
    return false;
  }

  public static void registerVpnStateListener (Runnable onChanged) {
    try {
      ConnectivityManager cm = (ConnectivityManager) AppContext.get().getSystemService(Context.CONNECTIVITY_SERVICE);
      if (cm == null) {
        return;
      }
      NetworkRequest request = new NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
        .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
        .build();
      cm.registerNetworkCallback(request, new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable (Network network) {
          UI.post(onChanged);
        }

        @Override
        public void onLost (Network network) {
          UI.post(onChanged);
        }
      });
    } catch (Throwable t) {
      Log.e("Unable to register VPN state listener", t);
    }
  }
}
