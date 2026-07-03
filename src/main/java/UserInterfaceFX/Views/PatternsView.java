package UserInterfaceFX.Views;

import AppLogic.AppController;
import AppLogic.DiffSession;
import AppLogic.GenerationContext;
import MapGeneration.GenerationElements.Pattern;
import UserInterfaceFX.PatternHeatmap;
import atlantafx.base.theme.Styles;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static DataManager.Parameters.logger;

/**
 * Pattern manager: load a generation pattern (.pat or existing difficulty) and visualize
 * it. Shows the row-normalized heatmap inline; the classic visualizations from the old
 * "Show patterns" window (heatmap variants, Dirichlet) open in their own windows.
 */
public class PatternsView extends VBox {

    private final AppController controller;
    private final Label patternInfo = new Label();
    private final Canvas heatmapCanvas = new Canvas(560, 420);
    private final Label result = new Label();

    public PatternsView(AppController controller, Stage stage) {
        super(12);
        this.controller = controller;
        setPadding(new Insets(16));

        Label title = new Label("Patterns");
        title.getStyleClass().add(Styles.TITLE_3);

        Label description = new Label(
                "The pattern steers which note follows which during generation (complex/sectioned). "
                        + "Load a .pat file or reuse the note transitions of an existing difficulty (.dat).");
        description.getStyleClass().add(Styles.TEXT_MUTED);
        description.setWrapText(true);
        description.setMinHeight(Region.USE_PREF_SIZE);

        Button load = new Button("Load pattern…");
        load.getStyleClass().add(Styles.ACCENT);
        load.setOnAction(e -> loadPattern(stage));

        patternInfo.getStyleClass().add(Styles.TEXT_MUTED);
        HBox loadBar = new HBox(12, load, patternInfo);
        loadBar.setAlignment(Pos.CENTER_LEFT);

        // Classic visualizations (open own windows, ported 1:1 from the old "Show patterns" frame)
        Map<String, BiConsumer<Pattern, String>> visualizations = new LinkedHashMap<>();
        visualizations.put("Heatmap", Pattern::visualizeAsHeatmap);
        visualizations.put("Truncated heatmap", Pattern::visualizeAsHeatmapTruncated);
        visualizations.put("Normalized heatmap", Pattern::visualizeAsHeatmapNormalized);
        visualizations.put("Log-normalized heatmap", Pattern::visualizeAsHeatmapNormalizedLogarithmically);
        visualizations.put("Dirichlet-multinomial distribution", Pattern::visualizeDirichletMultinomialDistribution);

        HBox visualizationBar = new HBox(8);
        visualizationBar.setAlignment(Pos.CENTER_LEFT);
        visualizations.forEach((name, visualization) -> {
            Button button = new Button(name);
            button.setOnAction(e -> visualize(name, visualization));
            visualizationBar.getChildren().add(button);
        });

        Label heatmapCaption = new Label("Row-normalized transition probabilities (row = current note, column = next note, stronger blue = more likely):");
        heatmapCaption.getStyleClass().add(Styles.TEXT_MUTED);
        heatmapCaption.setWrapText(true);
        heatmapCaption.setMinHeight(Region.USE_PREF_SIZE);

        result.setWrapText(true);
        result.setMinHeight(Region.USE_PREF_SIZE);

        ScrollPane scroll = new ScrollPane(new VBox(8, heatmapCaption, heatmapCanvas));
        scroll.setFitToWidth(true);

        getChildren().addAll(title, description, loadBar, visualizationBar, scroll, result);
        refresh();

        // Pattern changes have no own event; refresh alongside session events (e.g. after
        // a pattern was loaded through the Generate view and a generation ran).
        controller.addListener(new AppController.Listener() {
            @Override
            public void onStateChanged(AppLogic.AppState state) {
                javafx.application.Platform.runLater(PatternsView.this::refresh);
            }

            @Override
            public void onActiveDiffChanged(DiffSession activeDiff) {
                javafx.application.Platform.runLater(PatternsView.this::refresh);
            }
        });
    }

    private void loadPattern(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load pattern");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Patterns & difficulties (*.pat, *.dat, *.json)", "*.pat", "*.dat", "*.json"));
        File file = chooser.showOpenDialog(stage);
        if (file == null) return;

        try {
            controller.loadPatternFromFile(file);
            result.setText("✓ Pattern loaded from " + file.getName());
            refresh();
        } catch (Exception ex) {
            logger.error("Could not load pattern: {}", ex.getMessage());
            result.setText("Failed to load pattern: " + ex.getMessage());
        }
    }

    /** Opens one of the classic visualization windows, honoring the active diff's variance. */
    private void visualize(String name, BiConsumer<Pattern, String> visualization) {
        Pattern pattern = controller.getPattern();
        if (pattern == null) {
            result.setText("No pattern loaded yet — load one first.");
            return;
        }

        DiffSession active = controller.getActiveDiff();
        int variance = active == null ? 0 : active.getPatternVariance() * 10;
        GenerationContext.patternVariance = variance;
        try {
            String windowTitle = "Pattern visualized as a " + name
                    + (variance != 0 ? " with a variance of " + (variance / 10) : "");
            visualization.accept(Pattern.adjustVariance(pattern), windowTitle);
        } finally {
            GenerationContext.patternVariance = 0;
        }
    }

    private void refresh() {
        Pattern pattern = controller.getPattern();
        patternInfo.setText(pattern == null
                ? "No pattern loaded — generators fall back to the default pattern."
                : "Pattern loaded (" + countPatternRows(pattern) + " note transitions).");
        PatternHeatmap.draw(heatmapCanvas, pattern);
    }

    private static int countPatternRows(Pattern pattern) {
        int size = 0;
        while (size < pattern.patterns.length && pattern.patterns[size][0] != null) size++;
        return size;
    }
}
