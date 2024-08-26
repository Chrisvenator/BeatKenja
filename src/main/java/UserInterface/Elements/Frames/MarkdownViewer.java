package UserInterface.Elements.Frames;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

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
        String htmlContent = renderMarkdownToHtml(markdownContent);

        // Display the HTML content in a JEditorPane
        JEditorPane editorPane = new JEditorPane("text/html", htmlContent);
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        editorPane.setText(htmlContent);

        // Add the editor pane to a scroll pane
        JScrollPane scrollPane = new JScrollPane(editorPane);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
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
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }
}
