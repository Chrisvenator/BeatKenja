package UserInterfaceFX.Views;

import AppLogic.AppController;
import AppLogic.DiffSession;
import AppLogic.GenerationContext;
import AppLogic.GeneratorType;
import DataManager.Parameters;
import atlantafx.base.theme.Styles;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.List;
import java.util.Random;

import static DataManager.Parameters.logger;

/**
 * Step 3: pick a generator and run it on the active diff (or all diffs).
 * Generator cards on the left, parameter panel (pattern, variance, seed, flags) on the right.
 * Generation runs in a background Task so the UI stays responsive; progress and result
 * are shown at the bottom. Variance is stored per diff (tabs), matching the plan.
 */
public class GenerateView extends VBox {

    private final AppController controller;
    private final Stage stage;

    private final Label activeDiffLabel = new Label();
    private final Label patternLabel = new Label("Pattern: default");
    private final Slider varianceSlider = new Slider(-50, 50, 0);
    private final TextField seedField = new TextField();
    private final CheckBox oneHanded = new CheckBox("One-handed (Linear/Complex only)");
    private final CheckBox ignoreDDs = new CheckBox("Ignore DDs");
    private final ProgressBar progress = new ProgressBar();
    private final Label result = new Label();
    private final VBox cardsBox = new VBox(12);

    private final Label varianceValue = new Label("0");

    // Held separately so the model-readiness poller can enable/disable them
    private Button styleAwareActive;
    private Button styleAwareAll;
    private Label styleAwareStatus;
    private Region statusDot;

    public GenerateView(AppController controller, Stage stage) {
        super(12);
        this.controller = controller;
        this.stage = stage;
        setPadding(new Insets(16));

        activeDiffLabel.getStyleClass().add(Styles.TEXT_MUTED);

        for (GeneratorType type : GeneratorType.values()) {
            if (type == GeneratorType.STYLE_AWARE) {
                cardsBox.getChildren().add(buildStyleAwareCard());
                continue; // custom card added below
            }
            cardsBox.getChildren().add(card(type));
        }

        startModelReadinessPoller();

        javafx.scene.control.ScrollPane cardsScroll = new javafx.scene.control.ScrollPane(cardsBox);
        cardsScroll.setFitToWidth(true);
        cardsScroll.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        cardsScroll.getStyleClass().add(Styles.FLAT);

        HBox content = new HBox(16, cardsScroll, buildParameterPanel());
        HBox.setHgrow(cardsScroll, Priority.ALWAYS);
        javafx.scene.layout.VBox.setVgrow(content, Priority.ALWAYS);

        progress.setVisible(false);
        progress.setMaxWidth(Double.MAX_VALUE);
        result.setWrapText(true);
        result.setMinHeight(Region.USE_PREF_SIZE);

        getChildren().addAll(activeDiffLabel, content, progress, result);
        refreshActiveDiff();

        controller.addListener(new AppController.Listener() {
            @Override
            public void onActiveDiffChanged(DiffSession activeDiff) {
                Platform.runLater(GenerateView.this::refreshActiveDiff);
            }
        });
    }

    private VBox card(GeneratorType type) {
        Label title = new Label(type.label);
        title.getStyleClass().add(Styles.TITLE_4);

        Label description = new Label(type.description);
        description.getStyleClass().add(Styles.TEXT_MUTED);
        description.setWrapText(true);
        description.setMinHeight(Region.USE_PREF_SIZE);

        Button generateActive = new Button("Generate (active diff)");
        generateActive.getStyleClass().add(Styles.ACCENT);
        generateActive.setOnAction(e -> run(type, List.of(controller.getActiveDiff())));

        Button generateAll = new Button("Apply to all diffs");
        generateAll.getStyleClass().add(Styles.FLAT);
        generateAll.setOnAction(e -> run(type, List.copyOf(controller.session().diffs())));

        VBox box = new VBox(8, title, description, new HBox(8, generateActive, generateAll));
        box.setPadding(new Insets(14));
        box.getStyleClass().add("bk-card");
        return box;
    }

    private VBox buildParameterPanel() {
        Label title = new Label("PARAMETERS");
        title.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED);

        patternLabel.setWrapText(true);
        patternLabel.setMinHeight(Region.USE_PREF_SIZE);
        Button loadPattern = new Button("Load pattern (.pat / .dat)…");
        loadPattern.setOnAction(e -> loadPattern());

        Label varianceLabel = new Label("Pattern variance (per diff)");
        varianceLabel.setTooltip(new Tooltip("Low = repetitive but safe. High = varied but more parity breaks."));
        varianceSlider.setShowTickMarks(true);
        varianceSlider.setShowTickLabels(true);
        varianceSlider.setMajorTickUnit(25);
        varianceSlider.valueProperty().addListener((obs, o, n) -> {
            int v = n.intValue();
            DiffSession active = controller.getActiveDiff();
            if (active != null) active.setPatternVariance(v);
            varianceValue.setText((v >= 0 ? "+" : "") + v);
            varianceValue.getStyleClass().removeAll(Styles.SUCCESS, Styles.DANGER);
            if (v > 0) varianceValue.getStyleClass().add(Styles.SUCCESS);
            else if (v < 0) varianceValue.getStyleClass().add(Styles.DANGER);
        });

        varianceValue.setPrefWidth(40);
        varianceValue.setAlignment(Pos.CENTER_RIGHT);
        HBox varianceRow = new HBox(8, varianceSlider, varianceValue);
        HBox.setHgrow(varianceSlider, Priority.ALWAYS);

        Label seedLabel = new Label("Seed");
        seedField.setText(String.valueOf(Parameters.SEED));
        Button randomizeSeed = new Button("↻");
        randomizeSeed.setTooltip(new Tooltip("New random seed"));
        randomizeSeed.setOnAction(e -> seedField.setText(String.valueOf((long) (new Random().nextDouble() * 1_000_000_000))));

        ignoreDDs.setSelected(Parameters.ignoreDDs);
        ignoreDDs.selectedProperty().addListener((obs, o, n) -> Parameters.ignoreDDs = n);

        HBox seedRow = new HBox(6, seedField, randomizeSeed);
        HBox.setHgrow(seedField, Priority.ALWAYS);

        VBox panel = new VBox(10, title, patternLabel, loadPattern, varianceLabel, varianceRow,
                new javafx.scene.control.Separator(),
                seedLabel, seedRow, oneHanded, ignoreDDs);
        panel.setPadding(new Insets(14));
        panel.setPrefWidth(280);
        panel.getStyleClass().add("bk-card");
        return panel;
    }

    private void loadPattern() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a pattern file or difficulty");
        File patternDir = new File(Parameters.DEFAULT_PATTERN_FOLDER_PATH);
        if (patternDir.isDirectory()) chooser.setInitialDirectory(patternDir);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pattern (*.pat) or difficulty (*.dat, *.json)", "*.pat", "*.dat", "*.json"));
        File file = chooser.showOpenDialog(stage);
        if (file == null) return;

        try {
            controller.loadPatternFromFile(file);
            patternLabel.setText("Pattern: " + file.getName());
            patternLabel.getStyleClass().remove(Styles.DANGER);
        } catch (Exception ex) {
            logger.error("Error while loading pattern: {}", ex.getMessage());
            patternLabel.setText("Pattern load failed: " + ex.getMessage());
            patternLabel.getStyleClass().add(Styles.DANGER);
        }
    }

    /** Runs the generator in a background task; the seed field is applied to the global RNG first. */
    private void run(GeneratorType type, List<DiffSession> targets) {
        if (targets.isEmpty() || targets.getFirst() == null) return;
        applySeed();

        setDisable(true);
        progress.setVisible(true);
        progress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        result.setText("Generating " + type.label + " for " + targets.size() + " diff(s)…");
        result.getStyleClass().removeAll(Styles.DANGER, Styles.SUCCESS);

        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() {
                return controller.generateFor(type, oneHanded.isSelected(), targets);
            }
        };
        task.setOnSucceeded(e -> {
            List<String> errors = task.getValue();
            setDisable(false);
            progress.setVisible(false);
            if (errors.isEmpty()) {
                result.setText("✓ " + type.label + " generated for " + targets.size() + " diff(s). Check 4 · Review, then save under 5 · Export — nothing is on disk yet!");
                result.getStyleClass().add(Styles.SUCCESS);
            } else {
                result.setText("Finished with errors:\n" + String.join("\n", errors));
                result.getStyleClass().add(Styles.DANGER);
            }
        });
        task.setOnFailed(e -> {
            setDisable(false);
            progress.setVisible(false);
            result.setText("Generation failed: " + task.getException().getMessage());
            result.getStyleClass().add(Styles.DANGER);
        });
        new Thread(task, "generation").start();
    }

    private void applySeed() {
        try {
            Parameters.SEED = Long.parseLong(seedField.getText().trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid seed '{}', keeping previous seed {}", seedField.getText(), Parameters.SEED);
            seedField.setText(String.valueOf(Parameters.SEED));
        }
        Parameters.RANDOM = new Random(Parameters.SEED);
    }

    private VBox buildStyleAwareCard() {
        Label title = new Label(GeneratorType.STYLE_AWARE.label);
        title.getStyleClass().add(Styles.TITLE_4);

        Label description = new Label(GeneratorType.STYLE_AWARE.description);
        description.getStyleClass().add(Styles.TEXT_MUTED);
        description.setWrapText(true);
        description.setMinHeight(Region.USE_PREF_SIZE);

        statusDot = new Region();
        statusDot.getStyleClass().addAll("bk-status-dot", "bk-status-dot-loading");

        styleAwareStatus = new Label("Loading style model…");
        styleAwareStatus.getStyleClass().add(Styles.TEXT_MUTED);

        HBox statusRow = new HBox(6, statusDot, styleAwareStatus);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        styleAwareActive = new Button("Generate (active diff)");
        styleAwareActive.getStyleClass().add(Styles.ACCENT);
        styleAwareActive.setDisable(true);
        styleAwareActive.setOnAction(e -> run(GeneratorType.STYLE_AWARE, List.of(controller.getActiveDiff())));

        styleAwareAll = new Button("Apply to all diffs");
        styleAwareAll.getStyleClass().add(Styles.FLAT);
        styleAwareAll.setDisable(true);
        styleAwareAll.setOnAction(e -> run(GeneratorType.STYLE_AWARE, List.copyOf(controller.session().diffs())));

        VBox box = new VBox(8, title, description, statusRow,
                new HBox(8, styleAwareActive, styleAwareAll));
        box.setPadding(new Insets(14));
        box.getStyleClass().add("bk-card");
        return box;
    }

    /**
     * Polls GenerationContext.styleSpace every 2 s until the engine-loader thread finishes.
     * Enables/disables the Style-Aware buttons and updates the status label accordingly.
     */
    private void startModelReadinessPoller() {
        Timeline[] holder = new Timeline[1];
        holder[0] = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            MapGeneration.StyleSpace.StyleSpace ss = GenerationContext.styleSpace;
            boolean ready = ss != null && !ss.getArchetypes().isEmpty();
            styleAwareActive.setDisable(!ready);
            styleAwareAll.setDisable(!ready);
            statusDot.getStyleClass().removeAll("bk-status-dot-loading", "bk-status-dot-ready", "bk-status-dot-error");
            if (ready) {
                statusDot.getStyleClass().add("bk-status-dot-ready");
                styleAwareStatus.setText("Model ready — " + ss.getArchetypes().size() + " style archetypes loaded.");
                styleAwareStatus.getStyleClass().remove(Styles.DANGER);
                holder[0].stop();
            } else if (ss != null) {
                // styleSpace set but no archetypes — model file missing or empty
                statusDot.getStyleClass().add("bk-status-dot-error");
                styleAwareStatus.setText("Model loaded but empty — run StyleSpaceTrainer to generate archetypes.");
                styleAwareStatus.getStyleClass().add(Styles.DANGER);
                holder[0].stop();
            } else {
                statusDot.getStyleClass().add("bk-status-dot-loading");
            }
            // ss==null → engine-loader still running; keep polling
        }));
        holder[0].setCycleCount(Timeline.INDEFINITE);
        holder[0].play();
    }

    private void refreshActiveDiff() {
        DiffSession active = controller.getActiveDiff();
        if (active == null) {
            activeDiffLabel.setText("No diff selected.");
        } else {
            activeDiffLabel.setText("Active diff: " + active.difficultyFileName() + " (switch via the tabs above)");
            int v = active.getPatternVariance();
            varianceSlider.setValue(v);
            varianceValue.setText((v >= 0 ? "+" : "") + v);
            varianceValue.getStyleClass().removeAll(Styles.SUCCESS, Styles.DANGER);
            if (v > 0) varianceValue.getStyleClass().add(Styles.SUCCESS);
            else if (v < 0) varianceValue.getStyleClass().add(Styles.DANGER);
        }
    }
}