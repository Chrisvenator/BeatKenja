package UserInterface.Elements.Frames;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.List;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static DataManager.Parameters.logger;

public class MarkdownViewer extends JFrame {
    public MarkdownViewer(String filePath) {
        setTitle("Markdown Viewer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Load and parse the Markdown file
        String markdownContent = loadMarkdown(filePath);
        String htmlFragment = renderMarkdownToHtml(markdownContent);
        String fullHtml = wrapHtml(htmlFragment, filePath);

        // Embed a JavaFX WebView so full HTML/CSS and (animated) images render.
        // Constructing the JFXPanel bootstraps the FX runtime on first use.
        JFXPanel fxPanel = new JFXPanel();
        add(fxPanel, BorderLayout.CENTER);
        setVisible(true);

        // Keep the FX toolkit alive after this window is disposed, so re-opening works.
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            WebView webView = new WebView();
            webView.getEngine().loadContent(fullHtml, "text/html");
            fxPanel.setScene(new Scene(webView));
        });
    }

    /**
     * Loads the markdown content from the specified file path.
     */
    private String loadMarkdown(String filePath) {
        try {
            return Files.readString(Path.of(filePath));
        }
        catch (IOException e) {
            e.printStackTrace();
            logger.error("Error while reading markdown file.");
            return "Error loading file: " + filePath;
        }
    }

    /**
     * Converts the Markdown content to HTML using the commonmark-java library.
     */
    private String renderMarkdownToHtml(String markdown) {
        List<Extension> extensions = List.of(TablesExtension.create(), StrikethroughExtension.create());
        Parser parser = Parser.builder().extensions(extensions).build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
        return renderer.render(document);
    }

    /**
     * Wraps the rendered HTML fragment in a full document with a {@code <base href>}
     * pointing at the markdown file's directory, so relative image paths
     * (e.g. {@code ./assets/foo.gif}) resolve correctly in the WebView.
     */
    private String wrapHtml(String htmlFragment, String filePath) {
        String baseUri = Path.of(filePath).toAbsolutePath().getParent().toUri().toString();
        return "<!DOCTYPE html><html><head>"
                + "<base href=\"" + baseUri + "\">"
                + "<meta charset=\"UTF-8\">"
                + "</head><body>" + htmlFragment + "</body></html>";
    }
}