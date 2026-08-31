package UserInterfaceFX.Views;

import AppLogic.AppController;
import AppLogic.AppState;
import AppLogic.AudioPreviewPlayer;
import AppLogic.ClickTrackRenderer;
import AppLogic.DiffSession;
import AppLogic.SectionAnalysisService;
import AppLogic.SectionAnalysisService.SectionAnalysis;
import BeatSaberObjects.Objects.Enums.ParityErrorEnum;
import BeatSaberObjects.Objects.Note;
import UserInterfaceFX.Components.TimelineStrip;
import UserInterfaceFX.Components.TransportBar;
import UserInterfaceFX.Viewer.NoteField3D;
import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Pair;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static DataManager.Parameters.logger;

/**
 * Viewer tab: first-person 3D note lane synced to audio playback.
 * <p>
 * Shows notes flying toward the camera down the 4×3 Beat Saber grid, a scrub slider,
 * volume control, click-track toggle (notes or onsets), and a section timeline strip.
 * Analysis results are stored on the session so the Timing tab benefits too.
 * Read-only — no note editing in v1.
 */
public class ViewerView extends VBox implements UserInterfaceFX.AudioView {

    private final AppController controller;

    // Audio — one session-owned player, shared with the Timing view and driven by the global spine.
    private final AudioPreviewPlayer player;
    private final TransportBar transport;
    private SectionAnalysis analysis;

    // Click-track mode: "notes" renders clicks at note beat positions; "onsets" uses detected onsets
    private boolean useNoteClicks = true;
    /** Cached note-click wav (invalidated when active diff or BPM changes). */
    private File noteClickWav;
    /** The diff for which noteClickWav was rendered (null = needs re-render). */
    private DiffSession noteClickDiff;

    // 3D field + timeline
    private final NoteField3D noteField = new NoteField3D();
    private final TimelineStrip timeline = new TimelineStrip(48);

    // NJS / NJO controls
    private static final double NJS_DEFAULT = 30.0;
    private static final double NJO_DEFAULT = 0.0;
    /** Seconds seeked per mouse-wheel notch when scrubbing the 3D lane. */
    private static final double SCROLL_STEP_SECONDS = 0.5;
    private double currentNjs = NJS_DEFAULT;
    private double currentNjoBeat = NJO_DEFAULT;
    private Note[] currentNotes;
    private double currentBpm;

    // Playback controls
    private final CheckBox clickCheckbox = new CheckBox("Clicks");
    private double playheadSeconds = -1;

    private final Label statusLabel = new Label("Load audio to preview the generated map in 3D.");

    public ViewerView(AppController controller) {
        super(10);
        this.controller = controller;
        this.player = controller.audioPlayer();
        this.transport = new TransportBar(player);
        setPadding(new Insets(16));

        statusLabel.getStyleClass().add(Styles.TEXT_MUTED);
        statusLabel.setWrapText(true);

        VBox.setVgrow(noteField, Priority.ALWAYS);
        getChildren().addAll(buildToolbar(), buildNjsNjoRow(), noteField, timeline, buildPlaybackRow(), statusLabel);
        installScrollSeek();

        controller.addListener(new AppController.Listener() {
            @Override public void onActiveDiffChanged(DiffSession activeDiff) {
                Platform.runLater(() -> rebuildField(activeDiff));
            }
            @Override public void onStateChanged(AppState state) {
                Platform.runLater(() -> rebuildField(controller.getActiveDiff()));
            }
            @Override public void onBpmChanged(double bpm) {
                // Note-click wav depends on BPM; invalidate so it re-renders on next toggle
                noteClickWav = null;
                noteClickDiff = null;
                Platform.runLater(() -> rebuildField(controller.getActiveDiff()));
            }
            @Override public void onSeekRequested(double seconds) {
                // Global spine (or a parity-marker jump) asked us to move the playhead
                Platform.runLater(() -> { if (player.isLoaded()) transport.seek(seconds); });
            }
        });

        currentBpm = controller.session().getBpm();
        rebuildField(controller.getActiveDiff());
    }

    // --- Toolbar ---

    private HBox buildToolbar() {
        Button analyzeBtn = new Button("Load & analyze audio…");
        analyzeBtn.getStyleClass().add(Styles.ACCENT);
        analyzeBtn.setOnAction(e -> chooseAndAnalyze());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(8, analyzeBtn, spacer);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    // --- NJS / NJO row ---

    /**
     * Sliders for Note Jump Speed (NJS) and Note Jump Offset (NJO).
     * NJS controls how fast notes fly toward the camera (world units/sec; higher = faster, notes
     * appear earlier). NJO is a beat offset that shifts how far ahead notes first spawn
     * (positive = farther ahead, negative = closer), without triggering a full rebuild.
     */
    private HBox buildNjsNjoRow() {
        // NJS: 1..60, default 30
        Label njsLabel = new Label("NJS: " + (int) NJS_DEFAULT);
        njsLabel.setMinWidth(60);
        njsLabel.getStyleClass().add(Styles.TEXT_MUTED);
        Slider njsSlider = new Slider(1, 60, NJS_DEFAULT);
        njsSlider.setMajorTickUnit(5);
        njsSlider.setMinorTickCount(4);
        njsSlider.setShowTickMarks(true);
        njsSlider.setPrefWidth(200);
        njsSlider.valueProperty().addListener((obs, old, v) -> {
            int njs = (int) Math.round(v.doubleValue());
            njsLabel.setText("NJS: " + njs);
            currentNjs = njs;
            noteField.setNjs(currentNjs, currentNotes, currentBpm);
        });

        // NJO: -2..+4 beats, default 0; shown in beats
        Label njoLabel = new Label("NJO: 0.0 beats");
        njoLabel.setMinWidth(100);
        njoLabel.getStyleClass().add(Styles.TEXT_MUTED);
        Slider njoSlider = new Slider(-2, 4, NJO_DEFAULT);
        njoSlider.setMajorTickUnit(1);
        njoSlider.setMinorTickCount(3);
        njoSlider.setShowTickMarks(true);
        njoSlider.setSnapToTicks(false);
        njoSlider.setPrefWidth(200);
        njoSlider.valueProperty().addListener((obs, old, v) -> {
            double njo = Math.round(v.doubleValue() * 4) / 4.0; // snap to 0.25 beat steps
            njoLabel.setText(String.format("NJO: %.2f beats", njo));
            currentNjoBeat = njo;
            double offsetSec = currentBpm > 0 ? njo / currentBpm * 60.0 : 0;
            noteField.setNjoOffsetSeconds(offsetSec);
        });

        Button resetBtn = new Button("Reset");
        resetBtn.getStyleClass().add(Styles.FLAT);
        resetBtn.setOnAction(e -> {
            njsSlider.setValue(NJS_DEFAULT);
            njoSlider.setValue(NJO_DEFAULT);
        });

        HBox row = new HBox(10, njsLabel, njsSlider, njoLabel, njoSlider, resetBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // --- Playback row ---

    private HBox buildPlaybackRow() {
        transport.setOnPlayhead(seconds -> {
            playheadSeconds = seconds;
            noteField.setPlayheadSeconds(seconds);
            timeline.setPlayheadSeconds(seconds);
        });

        // Click-track source: notes vs onsets
        ToggleGroup clickGroup = new ToggleGroup();
        RadioButton rbNotes   = new RadioButton("note clicks");
        RadioButton rbOnsets  = new RadioButton("onset clicks");
        rbNotes.setToggleGroup(clickGroup);
        rbOnsets.setToggleGroup(clickGroup);
        rbNotes.setSelected(true);
        rbNotes.setDisable(true);
        rbOnsets.setDisable(true);
        rbNotes.getStyleClass().add(Styles.TEXT_SMALL);
        rbOnsets.getStyleClass().add(Styles.TEXT_SMALL);

        clickCheckbox.setDisable(true);
        clickCheckbox.getStyleClass().add(Styles.TEXT_SMALL);
        clickCheckbox.setOnAction(e -> applyClickTrack());

        clickGroup.selectedToggleProperty().addListener((obs, old, sel) -> {
            useNoteClicks = sel == rbNotes;
            // Switching mode: disable click track and re-enable so it re-renders on next enable
            if (clickCheckbox.isSelected()) {
                player.setClickTrackEnabled(false);
                clickCheckbox.setSelected(false);
            }
            // Free the old rendered wav for the other mode (force re-render)
            if (useNoteClicks) {
                noteClickWav = null;
                noteClickDiff = null;
            }
            rbNotes.setDisable(false);
            rbOnsets.setDisable(false);
        });

        // Keep radio buttons enabled state in sync with clickCheckbox
        clickCheckbox.disableProperty().addListener((obs, old, disabled) -> {
            rbNotes.setDisable(disabled);
            rbOnsets.setDisable(disabled);
        });

        transport.setTrailing(clickCheckbox, rbNotes, rbOnsets);
        return transport;
    }

    // --- Audio analysis ---

    private void chooseAndAnalyze() {
        // If the session already has analysis from the Timing tab, offer to reuse it
        SectionAnalysis existing = controller.session().getSectionAnalysis();
        if (existing != null && existing.wavFile() != null && existing.wavFile().exists()) {
            applyAnalysis(existing);
            setStatus("Reused analysis from Timing tab: "
                    + existing.tiers().length + " sections · BPM estimate " + String.format("%.1f", existing.estimatedBpm()));
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose the song's audio file");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Audio (*.wav, *.mp3, *.ogg, *.egg)", "*.wav", "*.mp3", "*.ogg", "*.egg"));
        String folder = controller.session().getMapFolderPath();
        if (folder != null && new File(folder).isDirectory()) chooser.setInitialDirectory(new File(folder));
        File audio = chooser.showOpenDialog(getScene().getWindow());
        if (audio == null) return;

        setStatus("Analyzing " + audio.getName() + "…");
        disablePlayback();

        Task<SectionAnalysis> task = new Task<>() {
            @Override protected SectionAnalysis call() throws Exception {
                SectionAnalysis result = SectionAnalysisService.analyze(audio);
                try { player.load(result.wavFile()); }
                catch (Exception ex) { logger.warn("Audio preview unavailable: {}", ex.getMessage()); }
                return result;
            }
        };
        task.setOnSucceeded(e -> {
            applyAnalysis(task.getValue());
            controller.session().setSectionAnalysis(task.getValue());
            controller.notifyAnalysisChanged(); // refresh the global spine's heat-ribbon
            controller.notifyAudioChanged(); // shared player has a new song; re-sync other views
            setStatus(analysisSummary());
        });
        task.setOnFailed(e -> setStatus("✗ " + task.getException().getMessage()));
        new Thread(task, "viewer-analysis").start();
    }

    private void applyAnalysis(SectionAnalysis result) {
        analysis = result;
        // Load the wav if not already playing this file
        if (!player.isLoaded()) {
            try { player.load(result.wavFile()); }
            catch (Exception ex) { logger.warn("Audio load failed: {}", ex.getMessage()); }
        }
        timeline.setAnalysis(result);
        timeline.setShowDensity(true); // note-density ribbon over the heat-bands
        timeline.setOnSeek(transport::seek);
        refreshTimelineBookmarks();
        if (player.isLoaded()) {
            transport.onLoaded();
            clickCheckbox.setDisable(false);
        }
        // Invalidate note-click wav whenever we get new audio
        noteClickWav = null;
        noteClickDiff = null;
    }

    // --- Click track ---

    /**
     * Toggles click-track on/off, rendering the wav lazily on first enable.
     * Source: note beats (useNoteClicks=true) or detected onsets (false).
     */
    private void applyClickTrack() {
        if (!player.isLoaded() || analysis == null) { clickCheckbox.setSelected(false); return; }

        if (!clickCheckbox.isSelected()) {
            player.setClickTrackEnabled(false);
            return;
        }

        if (useNoteClicks) {
            DiffSession active = controller.getActiveDiff();
            // Re-render if diff or wav changed
            if (noteClickWav != null && active == noteClickDiff && player.hasClickTrack()) {
                player.setClickTrackEnabled(true);
                return;
            }
            renderNoteClicks(active);
        } else {
            // Onset clicks: same as TimingView (re-render only if not already available)
            if (player.hasClickTrack()) {
                player.setClickTrackEnabled(true);
                return;
            }
            renderOnsetClicks();
        }
    }

    private void renderNoteClicks(DiffSession diff) {
        if (diff == null || diff.map() == null || diff.map()._notes == null) {
            clickCheckbox.setSelected(false);
            setStatus("No notes in active diff.");
            return;
        }
        Note[] notes = diff.map()._notes;
        double bpm = controller.session().getBpm();
        if (bpm <= 0) { clickCheckbox.setSelected(false); return; }

        double[] noteSecs = Arrays.stream(notes)
                .mapToDouble(n -> n._time / bpm * 60.0)
                .sorted()
                .toArray();

        clickCheckbox.setDisable(true);
        setStatus("Rendering note clicks…");
        File wav = analysis.wavFile();
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                File clicked = ClickTrackRenderer.render(wav, noteSecs);
                player.loadClickTrack(clicked);
                noteClickWav = clicked;
                noteClickDiff = diff;
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            clickCheckbox.setDisable(false);
            player.setClickTrackEnabled(true);
            setStatus(analysisSummary());
        });
        task.setOnFailed(e -> {
            clickCheckbox.setDisable(false);
            clickCheckbox.setSelected(false);
            setStatus("✗ Click render failed: " + task.getException().getMessage());
        });
        new Thread(task, "viewer-note-clicks").start();
    }

    private void renderOnsetClicks() {
        clickCheckbox.setDisable(true);
        setStatus("Rendering onset clicks…");
        File wav = analysis.wavFile();
        double[] onsets = analysis.onsetTimesSeconds();
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                player.loadClickTrack(ClickTrackRenderer.render(wav, onsets));
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            clickCheckbox.setDisable(false);
            player.setClickTrackEnabled(true);
            setStatus(analysisSummary());
        });
        task.setOnFailed(e -> {
            clickCheckbox.setDisable(false);
            clickCheckbox.setSelected(false);
            setStatus("✗ Onset click render failed: " + task.getException().getMessage());
        });
        new Thread(task, "viewer-onset-clicks").start();
    }

    // --- Field rebuild ---

    private void rebuildField(DiffSession diff) {
        if (diff == null || diff.map() == null) {
            currentNotes = null;
            noteField.clear();
            return;
        }
        currentBpm   = controller.session().getBpm();
        currentNotes = diff.map()._notes;
        noteField.setNjs(currentNjs, currentNotes, currentBpm);
        // Re-apply NJO in case BPM changed (seconds conversion depends on BPM)
        double offsetSec = currentBpm > 0 ? currentNjoBeat / currentBpm * 60.0 : 0;
        noteField.setNjoOffsetSeconds(offsetSec);
        noteField.setPlayheadSeconds(Math.max(0, playheadSeconds));
        refreshFlaggedNotes(diff);
        // Invalidate note-click wav if the diff changed
        if (diff != noteClickDiff) {
            noteClickWav = null;
            noteClickDiff = null;
        }
        refreshTimelineBookmarks();
    }

    /** Shows the active diff's bookmarks as inline markers on the timeline (cleared if none / no BPM). */
    private void refreshTimelineBookmarks() {
        DiffSession active = controller.getActiveDiff();
        double bpm = controller.session().getBpm();
        boolean hasBookmarks = active != null && active.map() != null && bpm > 0
                && active.map().bookmarks != null && !active.map().bookmarks.isEmpty();
        timeline.setBookmarks(hasBookmarks ? active.map().bookmarks : java.util.List.of(), bpm);
        Note[] notes = (active != null && active.map() != null && bpm > 0) ? active.map()._notes : null;
        timeline.setNotes(notes, bpm);
    }

    /** Highlights the notes flagged by parity errors — the incorrect notes that get bookmarked. */
    private void refreshFlaggedNotes(DiffSession diff) {
        Set<Float> beats = new HashSet<>();
        if (diff != null) {
            for (Pair<Float, ParityErrorEnum> err : diff.parityErrors()) beats.add(err.getKey());
        }
        noteField.setFlaggedBeats(beats);
    }

    /** Mouse-wheel over the 3D lane scrubs the playhead (wheel up = forward, down = back). */
    private void installScrollSeek() {
        noteField.setOnScroll(e -> {
            if (!player.isLoaded()) return;
            double base = playheadSeconds < 0 ? 0 : playheadSeconds;
            double target = base + e.getDeltaY() / 40.0 * SCROLL_STEP_SECONDS;
            transport.seek(Math.max(0, Math.min(player.durationSeconds(), target)));
            e.consume();
        });
    }

    // --- Lifecycle ---

    /** Stops this view's transport timer on app shutdown; the shared player is closed by the controller. */
    public void shutdown() {
        transport.stopTimer();
    }

    @Override
    public void onShown() {
        // Catch up to the shared player (Timing may have loaded/scrubbed it) and resume ticking.
        transport.syncFromPlayer();
        player.setClickTrackEnabled(clickCheckbox.isSelected());
    }

    @Override
    public void onHidden() {
        // Playback keeps running (the global spine still tracks it); just stop this bar's own timer.
        transport.stopTimer();
    }

    @Override
    public void togglePlay() {
        transport.togglePlay();
    }

    // --- Helpers ---

    private void disablePlayback() {
        transport.reset();
        player.close();
        playheadSeconds = -1;
        clickCheckbox.setSelected(false);
        clickCheckbox.setDisable(true);
        noteClickWav = null;
        noteClickDiff = null;
    }

    private String analysisSummary() {
        if (analysis == null) return "";
        return String.format("%d sections · BPM estimate %.1f · %d onsets",
                analysis.tiers().length, analysis.estimatedBpm(), analysis.onsetTimesSeconds().length);
    }

    private void setStatus(String text) {
        statusLabel.setText(text);
    }
}
