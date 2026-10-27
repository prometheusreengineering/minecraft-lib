package ing.prometheusreengineer.minecraft.util;

import ing.prometheusreengineer.minecraft.log.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.Map;

/**
 * Small HTTP client on {@link HttpURLConnection}, which every JRE has.
 * <br>
 * Every request has connect and read timeouts ({@code -Dprometheus.http.timeout}, in ms, default 10s) and a
 * User-Agent, and any status of 400 or above is an {@link IOException}. Redirects are followed (GitHub release assets
 * redirect to their CDN).
 */
public final class Http {
    private Http() {}

    private static final Log log = Log.lib("Http");
    private static final String USER_AGENT = "Prometheus-Minecraft (+https://github.com/prometheusreengineering)";

    public static byte @NotNull [] get(@NotNull String url) throws IOException {
        return get(url, Collections.emptyMap());
    }

    public static byte @NotNull [] get(@NotNull String url, @NotNull Map<String, String> headers) throws IOException {
        HttpURLConnection connection = open(url, headers);
        try (InputStream in = connection.getInputStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            copy(in, out, null);
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    /**
     * UTF-8.
     */
    public static @NotNull String getString(@NotNull String url, @NotNull Map<String, String> headers) throws IOException {
        return new String(get(url, headers), StandardCharsets.UTF_8);
    }

    /**
     * Downloads into {@code target} atomically: an interrupted download (fewer bytes than {@code Content-Length}) or a
     * digest mismatch throws and leaves {@code target} as it was.
     *
     * @param sha256 the expected SHA-256 (see {@link Hashing#matches}), or null to skip the check
     */
    public static void download(@NotNull String url, @NotNull Path target, @Nullable String sha256) throws IOException {
        log.debug("downloading %s to %s", url, target);
        HttpURLConnection connection = open(url, Collections.emptyMap());
        try (InputStream in = connection.getInputStream()) {
            long length = connection.getContentLengthLong();
            AtomicFiles.write(target, out -> {
                MessageDigest digest = Hashing.digest("SHA-256");
                long copied = copy(in, out, digest);
                // a server hanging up early can look like a normal end of stream, so count
                if (length >= 0 && copied != length) {
                    throw new IOException("incomplete download from " + url + ": got " + copied + " of " + length + " bytes");
                }
                String actual = Hashing.hex(digest.digest());
                if (sha256 != null && !Hashing.matches(actual, sha256)) {
                    throw new IOException("sha256 mismatch for " + url + ": expected " + sha256 + ", got " + actual);
                }
            });
        } finally {
            connection.disconnect();
        }
    }

    private static @NotNull HttpURLConnection open(@NotNull String url, @NotNull Map<String, String> headers) throws IOException {
        URLConnection raw = new URL(url).openConnection();
        if (!(raw instanceof HttpURLConnection)) throw new IOException("not an http(s) url: " + url);
        HttpURLConnection connection = (HttpURLConnection) raw;

        int timeout = Settings.getInt("http.timeout", 10_000);
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", USER_AGENT);
        for (Map.Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }

        int status = connection.getResponseCode();
        if (status >= 400) {
            connection.disconnect();
            throw new IOException("HTTP " + status + " from " + url);
        }
        return connection;
    }

    private static long copy(@NotNull InputStream in, @NotNull OutputStream out, @Nullable MessageDigest digest) throws IOException {
        byte[] buffer = new byte[1 << 16];
        long total = 0;
        for (int n; (n = in.read(buffer)) != -1; ) {
            out.write(buffer, 0, n);
            if (digest != null) digest.update(buffer, 0, n);
            total += n;
        }
        return total;
    }
}
