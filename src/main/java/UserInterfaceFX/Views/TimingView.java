package UserInterfaceFX.Views;

import AppLogic.AppController;
import AppLogic.AppState;
import AppLogic.AudioPreviewPlayer;
import AppLogic.ClickTrackRenderer;
import AppLogic.DiffSession;
import AppLogic.SectionAnalysisService;
import AppLogic.SectionAnalysisService.SectionAnalysis;
import UserInterfaceFX.Components.TimelineStrip;
import UserInterfaceFX.Components.TransportBar;
import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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
 * draw detected sections (tier-colored), the novelty curve, onset ticks and inline bookmark
 * markers on a shared {@link TimelineStrip}, and optionally apply the sections as
 * SECTIONED-generator bookmarks — asking first if the map already has bookmarks (manual
 * bookmarks are never silently overwritten). An audio preview player
 * ({@link AudioPreviewPlayer}) with play/pause, a scrub slider and click-to-seek on the
 * timeline lets the user listen to what each detected section actually sounds like.
 */
public class TimingView extends VBox implements UserInterfaceFX.AudioView {

    private final AppController controller;
    private final Label activeDiffLabel = new Label();
    private final Label result = new Label();

    private final TimelineStrip timeline = new TimelineStrip(140);
    private final Label songMapStatus = new Label("No analysis yet.");
    private final Button applyBookmarksButton = new Button("Apply as bookmarks to active diff");
    private SectionAnalysis analysis;

    /** Shared with every view via the controller, so playback/seek is global. */
    private final AudioPreviewPlayer player;
    private final TransportBar transport;
    private final CheckBox clickTrackCheckbox = new CheckBox("Click on onsets");

    public TimingView(AppController controller) {
        super(16);
        this.controller = controller;
        // The one session-owned player, shared with the Viewer and driven by the global spine.
        this.player = controller.audioPlayer();
        this.transport = new TransportBar(player);
        setPadding(new Insets(16));

        activeDiffLabel.getStyleClass().add(Styles.TEXT_MUTED);

        VBox oneColorCard = card(
                "→ 1-color timing notes",
                "All notes become blue dot notes in the bottom-left corner. This is the required input format for the generators.",
                false, true);

        VBox twoColorCard = card(
                "→ 2-color timing notes",
                "Keeps red/blue split as dot notes. Likely broken. Use at your own risk!",
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

            @Override
            public void onSeekRequested(double seconds) {
                // Global spine (or a parity-marker jump) asked us to move the playhead
                Platform.runLater(() -> { if (player.isLoaded()) transport.seek(seconds); });
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
        box.getStyleClass().add("bk-card");
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

        clickTrackCheckbox.setDisable(true);
        clickTrackCheckbox.setOnAction(e -> toggleClickTrack());
        transport.setTrailing(clickTrackCheckbox);
        transport.setOnPlayhead(timeline::setPlayheadSeconds);

        timeline.setShowNovelty(true);
        timeline.setOnSeek(transport::seek);

        VBox box = new VBox(10, title, description, new HBox(8, analyze, applyBookmarksButton, songMapStatus),
                transport, timeline);
        box.setPadding(new Insets(16));
        box.getStyleClass().add("bk-card");
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
        transport.reset();
        player.close();
        timeline.clear();
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
            controller.session().setSectionAnalysis(analysis);
            controller.notifyAnalysisChanged(); // refresh the global spine's heat-ribbon
            songMapStatus.setText(analysisSummary());
            applyBookmarksButton.setDisable(false);
            if (player.isLoaded()) {
                transport.onLoaded();
                clickTrackCheckbox.setDisable(false);
            }
            controller.notifyAudioChanged(); // shared player has a new song; re-sync other views
            timeline.setAnalysis(analysis);
            showBookmarksOnTimeline();
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

    /** Stops this view's transport timer on app shutdown; the shared player is closed by the controller. */
    public void shutdown() {
        transport.stopTimer();
    }

    @Override
    public void onShown() {
        // Catch up to the shared player (another view may have loaded/scrubbed it) and resume ticking.
        transport.syncFromPlayer();
        player.setClickTrackEnabled(clickTrackCheckbox.isSelected());
    }

    @Override
    public void onHidden() {
        // Playback keeps running (the global spine still tracks it); just stop this bar's own timer.
        transport.stopTimer();
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
        showBookmarksOnTimeline();
        controller.notifyAnalysisChanged(); // spine re-reads the active diff's bookmarks
    }

    /** Shows the active diff's bookmarks as inline markers on the timeline (cleared if none / no BPM). */
    private void showBookmarksOnTimeline() {
        DiffSession active = controller.getActiveDiff();
        double bpm = controller.session().getBpm();
        boolean hasBookmarks = active != null && bpm > 0
                && active.map().bookmarks != null && !active.map().bookmarks.isEmpty();
        timeline.setBookmarks(hasBookmarks ? active.map().bookmarks : List.of(), bpm);
    }
}