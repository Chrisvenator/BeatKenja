package AppLogic;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

import static DataManager.Parameters.logger;

/**
 * Minimal local HTTP server that serves the exported map zip at /map.zip, so web-based
 * check tools (bs-parity, MapCheck) can load the current map via their URL parameter.
 *
 * Binds to 127.0.0.1 on an ephemeral port and sends permissive CORS headers — the
 * hosted tools run on a different origin and fetch the zip from the browser.
 */
public class MapZipServer {

    private HttpServer server;
    private Path zipPath;

    /** Starts (or restarts) serving the given zip. Returns the local URL of the zip. */
    public synchronized String serve(Path zip) throws IOException {
        this.zipPath = zip;
        if (server == null) {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/map.zip", exchange -> {
                byte[] bytes = Files.readAllBytes(zipPath);
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Content-Type", "application/zip");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(bytes);
                }
            });
            server.start();
            logger.info("Map zip server started on port {}", server.getAddress().getPort());
        }
        return url();
    }

    public synchronized String url() {
        return server == null ? null : "http://127.0.0.1:" + server.getAddress().getPort() + "/map.zip";
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }
}