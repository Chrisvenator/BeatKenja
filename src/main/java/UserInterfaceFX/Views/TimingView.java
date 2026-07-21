package UserInterfaceFX.Views;

import AppLogic.AppController;
import AppLogic.AppState;
import AppLogic.AudioPreviewPlayer;
import AppLogic.ClickTrackRenderer;
import AppLogic.DiffSession;
import AppLogic.SectionAnalysisService;
import AppLogic.SectionAnalysisService.SectionAnalysis;
import atlantafx.base.theme.Styles;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

import static DataManager.Parameters.logger;

/**
 * Step 2: convert loaded diffs to timing notes.
 * Two cards: 1-color (the format the generators expect) and 2-color (legacy, shaky).
 * Each card converts the active diff; "Apply to all diffs" converts every loaded diff.
 * <p>
 * Below them, the Song Map card: analyze the song's audio ({@link SectionAnalysisService}),
 * draw detected sections (tier-colored), the novelty curve and onset ticks, and optionally
 * apply the sections as SECTIONED-generator bookmarks — asking first if the map already
 * has bookmarks (manual bookmarks are never silently overwritten). An audio preview player
 * ({@link AudioPreviewPlayer}) with play/pause, a scrub slider and click-to-seek on the
 * canvas lets the user listen to what each detected section actually sounds like.
 */
public class TimingView extends VBox {

    private final AppController controller;
    private final Label activeDiffLabel = new Label();
    private final Label result = new Label();

    private final Canvas songMapCanvas = new Canvas(0, 140);
    private final Label songMapStatus = new Label("No analysis yet.");
    private final Button applyBookmarksButton = new Button("Apply as bookmarks to active diff");
    private SectionAnalysis analysis;

    private final AudioPreviewPlayer player = new AudioPreviewPlayer();
    private final Button playButton = new Button("▶");
    private final Slider positionSlider = new Slider(0, 1, 0);
    private final Label timeLabel = new Label("0:00 / 0:00");
    private final CheckBox clickTrackCheckbox = new CheckBox("Click on onsets");
    private double playheadSeconds = -1;
    private final AnimationTimer playheadTimer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            updatePlayhead();
        }
    };

    public TimingView(AppController controller) {
        super(16);
        this.controller = controller;
        setPadding(new Insets(16));

        activeDiffLabel.getStyleClass().add(Styles.TEXT_MUTED);

        VBox oneColorCard = card(
                "→ 1-color timing notes",
                "All notes become blue dot notes in the bottom-left corner. This is the required input format for the generators.",
                false, true);

        VBox twoColorCard = card(
                "→ 2-color timing notes",
                "Keeps red/blue split as dot notes. Likely broken (old UI warned as well) — use at your own risk.",
                true, false);

        HBox cards = new HBox(16, oneColorCard, twoColorCard);
        HBox.setHgrow(oneColorCard, Priority.ALWAYS);
        HBox.setHgrow(twoColorCard, Priority.ALWAYS);

        result.getStyleClass().add(Styles.SUCCESS);

        getChildren().addAll(activeDiffLabel, cards, songMapCard(), result);
        refreshActiveDiff();

        controller.addListener(new AppController.Listener() {
            @Override
            public void onActiveDiffChanged(DiffSession activeDiff) {
                Platform.runLater(TimingView.this::refreshActiveDiff);
            }

            @Override
            public void onStateChanged(AppState state) {
                Platform.runLater(TimingView.this::refreshActiveDiff);
            }
        });
    }

    private VBox card(String title, String description, boolean warning, boolean primary) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add(Styles.TITLE_4);

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add(warning ? Styles.WARNING : Styles.TEXT_MUTED);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMinHeight(Region.USE_PREF_SIZE);

        boolean oneColor = !warning;

        Button convertActive = new Button("Convert active diff");
        if (primary) convertActive.getStyleClass().add(Styles.ACCENT);
        convertActive.setOnAction(e -> convert(oneColor, List.of(controller.getActiveDiff())));

        Button convertAll = new Button("Apply to all diffs");
        convertAll.getStyleClass().add(Styles.FLAT);
        convertAll.setOnAction(e -> convert(oneColor, controller.session().diffs()));

        VBox box = new VBox(10, titleLabel, descriptionLabel, new HBox(8, convertActive, convertAll));
        box.setPadding(new Insets(16));
        box.setStyle("-fx-border-color: -color-border-default; -fx-border-width: 1; -fx-border-radius: 8;");
        return box;
    }

    private void convert(boolean oneColor, List<DiffSession> targets) {
        if (targets.isEmpty() || targets.get(0) == null) return;
        controller.convertToTimingNotes(oneColor, List.copyOf(targets));
        result.setText("✓ Converted " + targets.size() + " diff(s) to " + (oneColor ? "1-color" : "2-color") + " timing notes — continue with 3 · Generate");
    }

    private void refreshActiveDiff() {
        DiffSession active = controller.getActiveDiff();
        activeDiffLabel.setText(active == null
                ? "No diff selected."
                : "Active diff: " + active.difficultyFileName() + " (switch via the tabs above)");
    }

    // --- Song Map (sections) ---

    private VBox songMapCard() {
        Label title = new Label("Song Map — sections & intensity");
        title.getStyleClass().add(Styles.TITLE_4);

        Label description = new Label("Analyzes the song's audio: colored bands = detected sections "
                + "(blue calm → red peak), white line = structure-change novelty, ticks = onsets. "
                + "\"Apply\" writes them as SECTIONED-generator bookmarks (linear / 1-2 / complex / jumps).");
        description.getStyleClass().add(Styles.TEXT_MUTED);
        description.setWrapText(true);
        description.setMinHeight(Region.USE_PREF_SIZE);

        Button analyze = new Button("Analyze audio…");
        analyze.getStyleClass().add(Styles.ACCENT);
        analyze.setOnAction(e -> chooseAndAnalyze());

        applyBookmarksButton.setDisable(true);
        applyBookmarksButton.setOnAction(e -> applyBookmarks());

        songMapStatus.getStyleClass().add(Styles.TEXT_MUTED);

        playButton.setDisable(true);
        playButton.setOnAction(e -> togglePlayback());
        positionSlider.setDisable(true);
        positionSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(positionSlider, Priority.ALWAYS);
        positionSlider.valueProperty().addListener((obs, old, v) -> {
            if (positionSlider.isValueChanging()) scrubPreview(v.doubleValue());
        });
        positionSlider.setOnMouseReleased(e -> seekTo(positionSlider.getValue()));
        timeLabel.getStyleClass().add(Styles.TEXT_MUTED);

        Label volumeIcon = new Label("🔊");
        volumeIcon.getStyleClass().add(Styles.TEXT_MUTED);
        Slider volumeSlider = new Slider(0, 1, 1);
        volumeSlider.setPrefWidth(90);
        volumeSlider.setMinWidth(60);
        volumeSlider.valueProperty().addListener((obs, old, v) -> player.setVolume(v.doubleValue()));

        clickTrackCheckbox.setDisable(true);
        clickTrackCheckbox.setOnAction(e -> toggleClickTrack());

        HBox playbackRow = new HBox(8, playButton, positionSlider, timeLabel, volumeIcon, volumeSlider,
                clickTrackCheckbox);
        playbackRow.setAlignment(Pos.CENTER_LEFT);

        // The canvas must never drive the card's size: binding it straight to the card width
        // fed the canvas width back into the card's preferred width, growing the whole view a
        // pixel per layout pass. A holder pane with explicit pref width 0 breaks that loop.
        Pane canvasHolder = new Pane(songMapCanvas);
        canvasHolder.setMinSize(0, 140);
        canvasHolder.setPrefSize(0, 140);
        canvasHolder.setMaxHeight(140);
        songMapCanvas.widthProperty().bind(canvasHolder.widthProperty());
        songMapCanvas.heightProperty().bind(canvasHolder.heightProperty());
        songMapCanvas.widthProperty().addListener((obs, old, w) -> drawSongMap());
        songMapCanvas.setOnMouseClicked(e -> seekFromCanvas(e.getX()));

        VBox box = new VBox(10, title, description, new HBox(8, analyze, applyBookmarksButton, songMapStatus),
                playbackRow, canvasHolder);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-border-color: -color-border-default; -fx-border-width: 1; -fx-border-radius: 8;");
        return box;
    }

    private void chooseAndAnalyze() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose the song's audio file");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Audio (*.wav, *.mp3, *.ogg, *.egg)", "*.wav", "*.mp3", "*.ogg", "*.egg"));
        String folder = controller.session().getMapFolderPath();
        if (folder != null && new File(folder).isDirectory()) chooser.setInitialDirectory(new File(folder));
        File audio = chooser.showOpenDialog(getScene().getWindow());
        if (audio == null) return;

        songMapStatus.setText("Analyzing " + audio.getName() + "…");
        applyBookmarksButton.setDisable(true);
        playheadTimer.stop();
        player.close();
        playheadSeconds = -1;
        playButton.setText("▶");
        playButton.setDisable(true);
        positionSlider.setDisable(true);
        clickTrackCheckbox.setSelected(false);
        clickTrackCheckbox.setDisable(true);

        Task<SectionAnalysis> task = new Task<>() {
            @Override
            protected SectionAnalysis call() throws Exception {
                SectionAnalysis result = SectionAnalysisService.analyze(audio);
                try {
                    player.load(result.wavFile());
                } catch (Exception noPlayback) {
                    logger.warn("Audio preview unavailable: {}", noPlayback.getMessage());
                }
                return result;
            }
        };
        task.setOnSucceeded(e -> {
            analysis = task.getValue();
            songMapStatus.setText(analysisSummary());
            applyBookmarksButton.setDisable(false);
            if (player.isLoaded()) {
                playButton.setDisable(false);
                positionSlider.setDisable(false);
                positionSlider.setMax(player.durationSeconds());
                positionSlider.setValue(0);
                timeLabel.setText("0:00 / " + formatTime(player.durationSeconds()));
                clickTrackCheckbox.setDisable(false);
            }
            drawSongMap();
        });
        task.setOnFailed(e -> songMapStatus.setText("✗ " + task.getException().getMessage()));
        new Thread(task, "section-analysis").start();
    }

    // --- Audio preview playback ---

    private String analysisSummary() {
        return String.format("%d sections · BPM estimate %.1f · %d onsets",
                analysis.tiers().length, analysis.estimatedBpm(), analysis.onsetTimesSeconds().length);
    }

    /**
     * Toggles the onset click track. The first activation renders the clicks into a temp wav
     * in the background (a few seconds); afterwards toggling switches instantly in-place.
     */
    private void toggleClickTrack() {
        if (!player.isLoaded() || analysis == null) return;
        if (!clickTrackCheckbox.isSelected()) {
            player.setClickTrackEnabled(false);
            return;
        }
        if (player.hasClickTrack()) {
            player.setClickTrackEnabled(true);
            return;
        }

        clickTrackCheckbox.setDisable(true);
        songMapStatus.setText("Rendering onset clicks…");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                File clicked = ClickTrackRenderer.render(analysis.wavFile(), analysis.onsetTimesSeconds());
                player.loadClickTrack(clicked);
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            clickTrackCheckbox.setDisable(false);
            player.setClickTrackEnabled(true);
            songMapStatus.setText(analysisSummary());
        });
        task.setOnFailed(e -> {
            clickTrackCheckbox.setDisable(false);
            clickTrackCheckbox.setSelected(false);
            songMapStatus.setText("✗ Click render failed: " + task.getException().getMessage());
        });
        new Thread(task, "click-render").start();
    }

    private void togglePlayback() {
        if (!player.isLoaded()) return;
        if (player.isPlaying()) {
            player.pause();
            playheadTimer.stop();
            playButton.setText("▶");
            updatePlayhead();
        } else {
            player.play();
            playButton.setText("⏸");
            playheadTimer.start();
        }
    }

    /** While the slider is dragged, only the visuals follow — the seek happens on release. */
    private void scrubPreview(double seconds) {
        playheadSeconds = seconds;
        timeLabel.setText(formatTime(seconds) + " / " + formatTime(player.durationSeconds()));
        drawSongMap();
    }

    private void seekTo(double seconds) {
        if (!player.isLoaded()) return;
        player.seekSeconds(seconds);
        updatePlayhead();
    }

    private void seekFromCanvas(double x) {
        if (analysis == null || !player.isLoaded() || songMapCanvas.getWidth() <= 0) return;
        double seconds = x / songMapCanvas.getWidth() * analysis.durationSeconds();
        positionSlider.setValue(seconds);
        seekTo(seconds);
    }

    private void updatePlayhead() {
        playheadSeconds = player.positionSeconds();
        if (!positionSlider.isValueChanging()) positionSlider.setValue(playheadSeconds);
        timeLabel.setText(formatTime(playheadSeconds) + " / " + formatTime(player.durationSeconds()));
        if (!player.isPlaying()) {
            playheadTimer.stop();
            playButton.setText("▶");
        }
        drawSongMap();
    }

    private static String formatTime(double seconds) {
        int s = (int) Math.max(0, seconds);
        return String.format("%d:%02d", s / 60, s % 60);
    }

    /** Stops audio preview playback and releases the audio line (called on app shutdown). */
    public void shutdown() {
        playheadTimer.stop();
        player.close();
    }

    private void applyBookmarks() {
        DiffSession active = controller.getActiveDiff();
        if (analysis == null || active == null) {
            songMapStatus.setText("✗ Load a map and analyze audio first.");
            return;
        }
        double bpm = controller.session().getBpm();
        if (bpm <= 0) {
            songMapStatus.setText("✗ Map BPM unknown — load a map with a BPM (or set it) first.");
            return;
        }

        List<BeatSaberObjects.Objects.Bookmark> existing = active.map().bookmarks == null
                ? active.map().calculateBookmarks() : active.map().bookmarks;
        if (existing != null && !existing.isEmpty()) {
            Alert consent = new Alert(Alert.AlertType.CONFIRMATION,
                    "This diff already has " + existing.size() + " bookmark(s) — replace them with "
                            + (analysis.boundaries().size() + 1) + " generated section bookmarks?",
                    ButtonType.OK, ButtonType.CANCEL);
            consent.setHeaderText("Replace existing bookmarks?");
            if (consent.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        }

        active.map().bookmarks = new java.util.ArrayList<>(SectionAnalysisService.toBookmarks(analysis, bpm));
        songMapStatus.setText("✓ " + active.map().bookmarks.size()
                + " section bookmarks applied to " + active.difficultyFileName()
                + " — the SECTIONED generator will use them.");
    }

    private void drawSongMap() {
        GraphicsContext g = songMapCanvas.getGraphicsContext2D();
        double w = songMapCanvas.getWidth();
        double h = songMapCanvas.getHeight();
        g.clearRect(0, 0, w, h);
        if (analysis == null || w <= 0 || analysis.durationSeconds() <= 0) return;

        double pixelsPerSecond = w / analysis.durationSeconds();

        // Section bands, colored by intensity tier.
        for (int s = 0; s < analysis.tiers().length; s++) {
            double start = s == 0 ? 0 : analysis.boundaries().get(s - 1);
            double end = s == analysis.tiers().length - 1
                    ? analysis.durationSeconds() : analysis.boundaries().get(s);
            float[] tierColor = SectionAnalysisService.TIER_COLORS[analysis.tiers()[s]];
            g.setFill(Color.color(tierColor[0], tierColor[1], tierColor[2], 0.35));
            g.fillRect(start * pixelsPerSecond, 0, (end - start) * pixelsPerSecond, h);
        }

        // Onset ticks in the bottom strip.
        g.setStroke(Color.color(1, 1, 1, 0.25));
        g.setLineWidth(1);
        for (double t : analysis.onsetTimesSeconds()) {
            double x = t * pixelsPerSecond;
            g.strokeLine(x, h * 0.85, x, h);
        }

        // Novelty curve (scaled to its own max so shape stays readable).
        double maxNovelty = 1e-9;
        for (double v : analysis.novelty()) maxNovelty = Math.max(maxNovelty, v);
        g.setStroke(Color.color(1, 1, 1, 0.9));
        g.setLineWidth(1.5);
        g.beginPath();
        for (int i = 0; i < analysis.novelty().length; i++) {
            double x = analysis.noveltyTimesSeconds()[i] * pixelsPerSecond;
            double y = h * 0.8 * (1 - analysis.novelty()[i] / maxNovelty) + h * 0.02;
            if (i == 0) g.moveTo(x, y);
            else g.lineTo(x, y);
        }
        g.stroke();

        // Boundary lines.
        g.setStroke(Color.color(0, 0, 0, 0.65));
        g.setLineWidth(1.5);
        for (double b : analysis.boundaries()) {
            double x = b * pixelsPerSecond;
            g.strokeLine(x, 0, x, h);
        }

        // Playhead.
        if (playheadSeconds >= 0) {
            g.setStroke(Color.color(1.0, 0.84, 0.25, 0.95));
            g.setLineWidth(2);
            double x = playheadSeconds * pixelsPerSecond;
            g.strokeLine(x, 0, x, h);
        }
    }
}