package AppLogic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static DataManager.Parameters.logger;

/**
 * Manages a local copy of the ArcViewer desktop app (3D map previewer).
 *
 * ArcViewer is a Unity app, so it can't be embedded in JavaFX directly; instead the
 * latest Windows release is downloaded once from GitHub into ./tools/ArcViewer/ and
 * launched as an external process with the exported map zip as argument. Downloading
 * at runtime (instead of bundling) also keeps BeatKenja clear of GPL redistribution.
 */
public class ArcViewerManager {

    private static final String RELEASES_API = "https://api.github.com/repos/AllPoland/ArcViewer/releases/latest";
    private static final Path INSTALL_DIR = Path.of("./tools/ArcViewer");

    public static boolean isInstalled() {
        return findExecutable().isPresent();
    }

    /** The ArcViewer exe inside the install dir, if present. */
    public static Optional<File> findExecutable() {
        if (!Files.isDirectory(INSTALL_DIR)) return Optional.empty();
        try (var stream = Files.walk(INSTALL_DIR)) {
            return stream
                    .filter(p -> p.getFileName().toString().equalsIgnoreCase("ArcViewer.exe"))
                    .map(Path::toFile)
                    .findFirst();
        } catch (IOException e) {
            logger.error("Could not scan ArcViewer install dir: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Downloads and extracts the latest ArcViewer Windows release. Blocking — run in a
     * background task. Progress messages go to the given consumer.
     */
    public static void install(Consumer<String> status) throws Exception {
        status.accept("Fetching latest release info…");
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(new URI(RELEASES_API)).header("Accept", "application/vnd.github+json").build(),
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new IOException("GitHub API returned " + response.statusCode());

        JsonNode release = new ObjectMapper().readTree(response.body());
        JsonNode assets = release.get("assets");
        String downloadUrl = null;
        String assetName = null;
        for (JsonNode asset : assets) {
            String name = asset.get("name").asText().toLowerCase();
            if (name.endsWith(".zip") && (name.contains("windows") || name.contains("win"))) {
                downloadUrl = asset.get("browser_download_url").asText();
                assetName = asset.get("name").asText();
                break;
            }
        }
        if (downloadUrl == null) throw new IOException("No Windows zip asset found in latest ArcViewer release");

        status.accept("Downloading " + assetName + "…");
        HttpResponse<InputStream> download = client.send(
                HttpRequest.newBuilder(new URI(downloadUrl)).build(),
                HttpResponse.BodyHandlers.ofInputStream());
        if (download.statusCode() != 200) throw new IOException("Download failed with HTTP " + download.statusCode());

        status.accept("Extracting…");
        Files.createDirectories(INSTALL_DIR);
        try (ZipInputStream zip = new ZipInputStream(download.body())) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                Path target = INSTALL_DIR.resolve(entry.getName()).normalize();
                if (!target.startsWith(INSTALL_DIR.normalize())) continue; // zip-slip guard
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    try (FileOutputStream out = new FileOutputStream(target.toFile())) {
                        zip.transferTo(out);
                    }
                }
            }
        }

        logger.info("ArcViewer {} installed to {}", release.get("tag_name").asText(), INSTALL_DIR);
        status.accept("ArcViewer " + release.get("tag_name").asText() + " installed.");
    }

    /**
     * Launches ArcViewer with the given map zip. Whether the zip is auto-opened depends
     * on ArcViewer's CLI handling; if not, the user drags it in (the file sits right there).
     */
    public static void launch(File mapZip) throws IOException {
        File exe = findExecutable().orElseThrow(() -> new IOException("ArcViewer is not installed"));
        new ProcessBuilder(exe.getAbsolutePath(), mapZip.getAbsolutePath())
                .directory(exe.getParentFile())
                .start();
        logger.info("ArcViewer launched with {}", mapZip.getAbsolutePath());
    }
}