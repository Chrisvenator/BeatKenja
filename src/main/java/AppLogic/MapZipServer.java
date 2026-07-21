package AppLogic;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static DataManager.Parameters.logger;

/**
 * Local HTTP server that makes the web-based check tools (MapCheck, bs-parity) work with
 * the currently loaded map. Serves the exported map zip at /map.zip and reverse-proxies
 * the tool pages themselves, so page and zip share the same 127.0.0.1 origin.
 *
 * Why a proxy: both tools download "?url=..." maps through a public CORS proxy
 * (https://cors.bsmg.dev/), which cannot reach 127.0.0.1 — loading the zip URL directly on
 * the hosted pages therefore fails with 404. Serving the pages from this server removes all
 * cross-origin restrictions; for MapCheck the hardcoded CORS-proxy prefix is additionally
 * stripped from its JS bundle so it fetches the zip directly.
 */
public class MapZipServer {

    private static final String MAPCHECK_UPSTREAM = "https://kivalevan.me";
    private static final String MAPCHECK_PATH = "/BeatSaber-MapCheck/";
    private static final String BS_PARITY_UPSTREAM = "https://galaxymaster2.github.io";
    private static final String BS_PARITY_PATH = "/bs-parity/";
    /** Both protocol variants occur in the wild: MapCheck uses https, bs-parity http. */
    private static final String[] CORS_PROXY_PREFIXES = {"https://cors.bsmg.dev/", "http://cors.bsmg.dev/"};

    private HttpServer server;
    private Path zipPath;
    private final HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    private final Map<String, ProxyResponse> proxyCache = new ConcurrentHashMap<>();

    private record ProxyResponse(int status, String contentType, byte[] body) {
    }

    /** Starts (or restarts) serving the given zip. Returns the local URL of the zip. */
    public synchronized String serve(Path zip) throws IOException {
        this.zipPath = zip;
        ensureStarted();
        return url();
    }

    public synchronized String url() {
        return server == null ? null : "http://127.0.0.1:" + server.getAddress().getPort() + "/map.zip";
    }

    /** Local MapCheck page, preloaded with the served zip. Call after serve(). */
    public synchronized String mapCheckUrl() {
        return toolUrl(MAPCHECK_PATH);
    }

    /** Local bs-parity page, preloaded with the served zip. Call after serve(). */
    public synchronized String bsParityUrl() {
        return toolUrl(BS_PARITY_PATH);
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    private String toolUrl(String path) {
        if (server == null) return null;
        return "http://127.0.0.1:" + server.getAddress().getPort() + path
                + "?url=" + URLEncoder.encode(url(), StandardCharsets.UTF_8);
    }

    private void ensureStarted() throws IOException {
        if (server != null) return;
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/map.zip", this::handleZip);
        server.createContext(MAPCHECK_PATH, exchange -> handleProxy(exchange, MAPCHECK_UPSTREAM));
        server.createContext(BS_PARITY_PATH, exchange -> handleProxy(exchange, BS_PARITY_UPSTREAM));
        server.start();
        logger.info("Map zip server started on port {}", server.getAddress().getPort());
    }

    private void handleZip(HttpExchange exchange) throws IOException {
        Path zip = zipPath;
        if (zip == null || !Files.exists(zip)) {
            respond(exchange, 404, "text/plain", "No map is being served".getBytes(StandardCharsets.UTF_8));
            return;
        }
        respond(exchange, 200, "application/zip", Files.readAllBytes(zip));
    }

    /**
     * Fetches the requested path from the upstream tool host and relays it, caching per path.
     * The page's own query string (e.g. ?url=...) is intentionally not forwarded upstream —
     * it only matters to the JS running in the local page.
     */
    private void handleProxy(HttpExchange exchange, String upstreamHost) throws IOException {
        String path = exchange.getRequestURI().getPath();
        try {
            ProxyResponse response = proxyCache.computeIfAbsent(path, p -> fetchUpstream(upstreamHost, p));
            respond(exchange, response.status(), response.contentType(), response.body());
        } catch (Exception e) {
            logger.error("Proxying {} failed: {}", path, e.getMessage());
            respond(exchange, 502, "text/plain",
                    ("Could not reach " + upstreamHost + " — check your internet connection").getBytes(StandardCharsets.UTF_8));
        }
    }

    private ProxyResponse fetchUpstream(String upstreamHost, String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(upstreamHost + path)).GET().build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            String contentType = response.headers().firstValue("Content-Type").orElse(guessContentType(path));

            byte[] body = response.body();
            if (contentType.contains("javascript") || path.endsWith(".js")) {
                body = stripCorsProxy(body);
            }
            return new ProxyResponse(response.statusCode(), contentType, body);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes the public CORS-proxy prefix from tool JS, so "?url=" maps are fetched directly.
     * Same-origin here, thus no proxy needed — and the public proxy could never reach 127.0.0.1.
     */
    static byte[] stripCorsProxy(byte[] js) {
        String source = new String(js, StandardCharsets.UTF_8);
        for (String prefix : CORS_PROXY_PREFIXES) source = source.replace(prefix, "");
        return source.getBytes(StandardCharsets.UTF_8);
    }

    private static String guessContentType(String path) {
        if (path.endsWith(".js")) return "text/javascript";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".ico")) return "image/x-icon";
        return "text/html";
    }

    private static void respond(HttpExchange exchange, int status, String contentType, byte[] body) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
