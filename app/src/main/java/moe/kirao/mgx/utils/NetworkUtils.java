package moe.kirao.mgx.utils;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import me.vkryl.core.StringUtils;

public final class NetworkUtils {
  private NetworkUtils () {}

  /**
   * Resolves a host to a localized country name via RIPEstat (RIPE NCC) over HTTPS.
   */
  @WorkerThread
  @Nullable
  public static String resolveCountryForHost (String host) {
    if (StringUtils.isEmpty(host)) return null;
    HttpURLConnection conn = null;
    try {
      InetAddress addr = InetAddress.getByName(host.trim());
      if (addr.isAnyLocalAddress() || addr.isLoopbackAddress() || addr.isLinkLocalAddress() || addr.isSiteLocalAddress())
        return null;
      URL url = new URL("https://stat.ripe.net/data/maxmind-geo-lite/data.json?sourceapp=moegramx&resource=" + addr.getHostAddress());
      conn = (HttpURLConnection) url.openConnection();
      conn.setConnectTimeout(4000);
      conn.setReadTimeout(4000);
      if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) return null;
      StringBuilder buf = new StringBuilder();
      try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = r.readLine()) != null) buf.append(line);
      }
      String code = new JSONObject(buf.toString())
        .getJSONObject("data")
        .getJSONArray("located_resources").getJSONObject(0)
        .getJSONArray("locations").getJSONObject(0)
        .getString("country");
      if (code.length() != 2) return null;
      String name = new Locale("", code).getDisplayCountry(Locale.getDefault());
      return name.equalsIgnoreCase(code) ? null : name;
    } catch (Throwable t) {
      return null;
    } finally {
      if (conn != null) conn.disconnect();
    }
  }
}
