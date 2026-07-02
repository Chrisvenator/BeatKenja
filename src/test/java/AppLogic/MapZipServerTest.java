package AppLogic;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class MapZipServerTest {

    private final MapZipServer server = new MapZipServer();

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void servesZipWithCorsHeaders(@TempDir Path tempDir) throws Exception {
        Path zip = tempDir.resolve("map.zip");
        Files.write(zip, new byte[]{1, 2, 3, 4});

        String url = server.serve(zip);
        assertThat(url).startsWith("http://127.0.0.1:");

        HttpResponse<byte[]> response = HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder(new URI(url)).build(), HttpResponse.BodyHandlers.ofByteArray());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).containsExactly(1, 2, 3, 4);
        assertThat(response.headers().firstValue("Access-Control-Allow-Origin")).contains("*");
    }

    @Test
    void servingAnotherZipReusesServer(@TempDir Path tempDir) throws Exception {
        Path first = tempDir.resolve("a.zip");
        Path second = tempDir.resolve("b.zip");
        Files.write(first, new byte[]{1});
        Files.write(second, new byte[]{2});

        String url1 = server.serve(first);
        String url2 = server.serve(second);
        assertThat(url2).isEqualTo(url1);

        HttpResponse<byte[]> response = HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder(new URI(url2)).build(), HttpResponse.BodyHandlers.ofByteArray());
        assertThat(response.body()).containsExactly(2);
    }
}