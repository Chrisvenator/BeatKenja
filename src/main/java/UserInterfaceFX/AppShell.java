package UserInterfaceFX;

import AppLogic.AppController;
import AppLogic.AppState;
import AppLogic.AudioPreviewPlayer;
import AppLogic.DiffSession;
import AppLogic.SectionAnalysisService.SectionAnalysis;
import BeatSaberObjects.Objects.Note;
import UserInterfaceFX.Components.TimelineStrip;
import UserInterfaceFX.Views.LoadView;
import UserInterfaceFX.Views.SettingsView;
import atlantafx.base.theme.Styles;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static DataManager.Parameters.logger;

/**
 * Main layout of the JavaFX UI: toolbar, workflow sidebar, swappable content area,
 * and a status bar with an expandable log drawer.
 *
 * The workflow steps 2-5 stay disabled until the AppController reports a loaded map;
 * enablement is event-driven via the controller's state listener (no polling).
 */
public class AppShell extends BorderPane {

    private final AppController controller;
    private final StackPane contentArea = new StackPane();
    private final Map<String, Node> views = new LinkedHashMap<>();
    private final Map<String, ToggleButton> navButtons = new LinkedHashMap<>();
    private final ToggleGroup navGroup = new ToggleGroup();

    private final Label statusLabel = new Label("No map loaded — start with 1 · Load");
    private final Label mapHeaderTitle = new Label("No map loaded");
    private final Label bpmLabel = new Label();
    private final Region dirtyDot = new Region();
    private final HBox diffChips = new HBox(6);

    /** Persistent song timeline shown under the header on every view; hidden until a song is analyzed. */
    private final TimelineStrip globalTimeline = new TimelineStrip(56);
    /** Last analysis put on the spine — lets us skip re-setAnalysis (which resets the playhead) on unrelated refreshes. */
    private SectionAnalysis lastSpineAnalysis;

    /** The currently displayed view, tracked so we can notify AudioViews as they are shown/hidden. */
    private Node currentView;
    /** Last playhead second pushed to the spine — guards the per-frame clock from redundant redraws. */
    private double lastSpinePlayhead = -1;

    /**
     * Drives the global spine's playhead straight from the session's shared player, so the spine
     * tracks playback (and scrubbing) regardless of which view is visible — even one with no audio.
     */
    private final AnimationTimer spineClock = new AnimationTimer() {
        @Override public void handle(long now) {
            AudioPreviewPlayer player = controller.audioPlayer();
            if (!player.isLoaded()) return;
            double pos = player.positionSeconds();
            if (pos != lastSpinePlayhead) {
                lastSpinePlayhead = pos;
                globalTimeline.setPlayheadSeconds(pos);
            }
        }
    };

    /** Workflow steps that need a loaded map before they make sense. */
    private static final String[] LOCKED_STEPS = {"2 · Timing", "3 · Generate", "4 · Review", "5 · Export", "Viewer", "NPS Overview", "Characteristics"};

    public AppShell(AppController controller, Stage stage) {
        this.controller = controller;
        FxLog.install();
        // Theme-aware accent: keys the saber-blue accent ramp in app.css to the active Primer theme
        getStyleClass().add(DataManager.Parameters.DARK_MODE ? "bk-theme-dark" : "bk-theme-light");

        registerViews(stage);
        setTop(buildToolBar());
        setLeft(buildSidebar());
        setCenter(buildCenter());
        setBottom(buildStatusBar());

        wireControllerEvents();
        spineClock.start(); // spine tracks the shared player from now on
        // Global transport shortcuts (Space / arrows / Home) attach once the scene exists.
        sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleTransportKey);
        });
        // Dev aid: -Dbk.view=<name> opens a specific view on startup (smoke screenshots)
        selectView(System.getProperty("bk.view", "1 · Load"));
    }

    private void registerViews(Stage stage) {
        views.put("1 · Load", new LoadView(controller, stage, this::refreshMapHeader, this::selectView));
        views.put("2 · Timing", new UserInterfaceFX.Views.TimingView(controller));
        views.put("3 · Generate", new UserInterfaceFX.Views.GenerateView(controller, stage));
        views.put("4 · Review", new UserInterfaceFX.Views.ReviewView(controller, this::selectView));
        views.put("5 · Export", new UserInterfaceFX.Views.ExportView(controller, stage));
        views.put("Viewer", new UserInterfaceFX.Views.ViewerView(controller));
        views.put("NPS Overview", new UserInterfaceFX.Views.NpsOverviewView(controller));
        views.put("Utilities", new UserInterfaceFX.Views.UtilitiesView(controller));
        views.put("Characteristics", new UserInterfaceFX.Views.CharacteristicsView(controller));
        views.put("Batch MP3", new UserInterfaceFX.Views.BatchMp3View());
        views.put("Patterns", new UserInterfaceFX.Views.PatternsView(controller, stage));
        views.put("Settings", new SettingsView());
    }

    private ToolBar buildToolBar() {
        Label title = new Label("⬡ BeatKenja");
        title.getStyleClass().add(Styles.TITLE_3);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button settings = new Button("Settings", new FontIcon(Feather.SETTINGS));
        settings.getStyleClass().add(Styles.BUTTON_OUTLINED);
        settings.setOnAction(e -> selectView("Settings"));

        return new ToolBar(title, spacer, settings);
    }

    private VBox buildSidebar() {
        VBox box = new VBox(4);
        box.setPadding(new Insets(12));
        box.setPrefWidth(170);

        box.getChildren().add(sectionLabel("WORKFLOW"));
        for (String name : new String[]{"1 · Load", "2 · Timing", "3 · Generate", "4 · Review", "5 · Export"}) {
            box.getChildren().add(navButton(name));
        }

        // Inspection views — need a loaded map (locked until then, see LOCKED_STEPS)
        box.getChildren().add(new Separator());
        box.getChildren().add(sectionLabel("ANALYZE"));
        for (String name : new String[]{"Viewer", "NPS Overview", "Characteristics"}) {
            box.getChildren().add(navButton(name));
        }

        // Standalone tools — usable without a loaded map
        box.getChildren().add(new Separator());
        box.getChildren().add(sectionLabel("TOOLS"));
        for (String name : new String[]{"Utilities", "Batch MP3", "Patterns"}) {
            box.getChildren().add(navButton(name));
        }

        // Workflow steps 2-5 and the analyze views unlock once a map is loaded
        for (String step : LOCKED_STEPS) navButtons.get(step).setDisable(true);
        return box;
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED);
        label.setPadding(new Insets(8, 0, 2, 4));
        return label;
    }

    private ToggleButton navButton(String name) {
        ToggleButton button = new ToggleButton(name);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.getStyleClass().add(Styles.FLAT);
        button.setToggleGroup(navGroup);
        button.setOnAction(e -> {
            if (!button.isSelected()) button.setSelected(true); // keep one selected
            showView(name);
        });
        navButtons.put(name, button);
        return button;
    }

    private VBox buildCenter() {
        mapHeaderTitle.getStyleClass().add(Styles.TITLE_4);
        bpmLabel.getStyleClass().addAll("bk-numeric", Styles.TEXT_MUTED);

        dirtyDot.getStyleClass().add("bk-dirty-dot");
        dirtyDot.setVisible(false);
        dirtyDot.setManaged(false);
        Tooltip.install(dirtyDot, new Tooltip("Unsaved — generated map not yet exported (5 · Export)"));

        HBox titleBox = new HBox(8, dirtyDot, mapHeaderTitle);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        HBox header = new HBox(16, titleBox, bpmLabel, diffChips);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 16, 12, 16));

        // Persistent song spine: clicking it seeks every loaded audio view (Timing / Viewer).
        globalTimeline.setOnSeek(controller::requestSeek);
        globalTimeline.setShowDensity(true); // note-density ribbon over the heat-bands
        globalTimeline.setVisible(false);
        globalTimeline.setManaged(false);
        VBox.setMargin(globalTimeline, new Insets(0, 16, 8, 16));

        contentArea.setPadding(new Insets(0, 16, 16, 16));
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        return new VBox(header, globalTimeline, new Separator(), contentArea);
    }

    private VBox buildStatusBar() {
        ListView<String> logView = new ListView<>(FxLog.lines());
        logView.setPrefHeight(160);
        logView.setVisible(false);
        logView.setManaged(false);

        FontIcon logIcon = new FontIcon(Feather.CHEVRON_UP);
        ToggleButton logToggle = new ToggleButton("Log", logIcon);
        logToggle.getStyleClass().add(Styles.FLAT);
        logToggle.setOnAction(e -> {
            boolean show = logToggle.isSelected();
            logView.setVisible(show);
            logView.setManaged(show);
            logIcon.setIconCode(show ? Feather.CHEVRON_DOWN : Feather.CHEVRON_UP);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(8, statusLabel, spacer, logToggle);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(6, 12, 6, 12));

        return new VBox(new Separator(), bar, logView);
    }

    private void wireControllerEvents() {
        controller.addListener(new AppController.Listener() {
            @Override
            public void onStateChanged(AppState state) {
                Platform.runLater(() -> {
                    boolean loaded = state != AppState.EMPTY;
                    for (String step : LOCKED_STEPS) navButtons.get(step).setDisable(!loaded);
                    statusLabel.setText(switch (state) {
                        case EMPTY -> "No map loaded — start with 1 · Load";
                        case LOADED -> "Map loaded — convert timings or generate";
                        case GENERATED -> "Map generated — review, then export (not saved yet!)";
                        case SAVED -> "Saved ✓";
                    });
                    // Dirty = generated but not yet exported to disk
                    boolean dirty = state == AppState.GENERATED;
                    dirtyDot.setVisible(dirty);
                    dirtyDot.setManaged(dirty);
                    refreshMapHeader();
                    refreshGlobalTimeline();
                });
            }

            @Override
            public void onBpmChanged(double bpm) {
                Platform.runLater(() -> {
                    refreshMapHeader();
                    refreshGlobalTimeline();
                });
            }

            @Override
            public void onActiveDiffChanged(DiffSession activeDiff) {
                Platform.runLater(() -> {
                    refreshMapHeader();
                    refreshGlobalTimeline();
                });
            }

            @Override
            public void onAnalysisChanged() {
                Platform.runLater(AppShell.this::refreshGlobalTimeline);
            }

            @Override
            public void onAudioChanged() {
                // Shared player loaded/unloaded a song: re-sync the visible view's transport + spine.
                Platform.runLater(() -> {
                    if (currentView instanceof AudioView av) av.onShown();
                    lastSpinePlayhead = -1;
                    refreshGlobalTimeline();
                });
            }
        });
    }

    /**
     * Syncs the persistent song spine with the session: shows it once an analysis exists, draws the
     * heat-ribbon and the active diff's bookmark markers. Only re-sets the analysis when it actually
     * changed, so routine refreshes (BPM, diff switch) don't reset the live playhead.
     */
    private void refreshGlobalTimeline() {
        SectionAnalysis analysis = controller.session().getSectionAnalysis();
        boolean show = analysis != null;
        globalTimeline.setVisible(show);
        globalTimeline.setManaged(show);
        if (!show) {
            globalTimeline.clear();
            lastSpineAnalysis = null;
            return;
        }
        if (analysis != lastSpineAnalysis) {
            globalTimeline.setAnalysis(analysis);
            lastSpineAnalysis = analysis;
        }
        DiffSession active = controller.getActiveDiff();
        double bpm = controller.session().getBpm();
        boolean hasBookmarks = active != null && active.map() != null && bpm > 0
                && active.map().bookmarks != null && !active.map().bookmarks.isEmpty();
        globalTimeline.setBookmarks(hasBookmarks ? active.map().bookmarks : List.of(), bpm);
        Note[] notes = (active != null && active.map() != null && bpm > 0) ? active.map()._notes : null;
        globalTimeline.setNotes(notes, bpm);
    }

    /** Updates the map header (folder name + diff chips) from the controller session. */
    private void refreshMapHeader() {
        String folder = controller.session().getMapFolderPath();
        if (folder == null || controller.maps().isEmpty()) {
            mapHeaderTitle.setText("No map loaded");
            bpmLabel.setText("");
            diffChips.getChildren().clear();
            return;
        }

        String songName = folder.replace('\\', '/');
        songName = songName.substring(songName.lastIndexOf('/') + 1);
        mapHeaderTitle.setText(songName);
        bpmLabel.setText("BPM " + controller.session().getBpm());

        // Diff tabs: the selected one is the diff all step views operate on
        diffChips.getChildren().clear();
        ToggleGroup chipGroup = new ToggleGroup();
        controller.session().diffs().forEach(diff -> {
            ToggleButton chip = new ToggleButton(diff.difficultyFileName().replace(".dat", ""));
            chip.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.ACCENT);
            chip.setToggleGroup(chipGroup);
            chip.setSelected(diff == controller.getActiveDiff());
            chip.setOnAction(e -> {
                chip.setSelected(true); // keep one selected
                controller.setActiveDiff(diff);
            });

            Button deleteBtn = new Button(null, new FontIcon(Feather.X));
            deleteBtn.getStyleClass().addAll(Styles.FLAT, Styles.DANGER, Styles.SMALL);
            deleteBtn.setTooltip(new javafx.scene.control.Tooltip("Remove this diff from the session (does not delete the file)"));
            deleteBtn.setAccessibleText("Remove " + diff.difficultyFileName() + " from session");
            deleteBtn.setOnAction(e -> controller.unloadDiff(diff.difficultyFileName()));

            // Merge chip label and delete button visually into one pill
            HBox chipRow = new HBox(0, chip, deleteBtn);
            chipRow.setAlignment(Pos.CENTER_LEFT);
            chipRow.getStyleClass().add("bk-diff-chip");
            diffChips.getChildren().add(chipRow);
        });
    }

    private void selectView(String name) {
        ToggleButton button = navButtons.get(name);
        if (button != null) button.setSelected(true);
        showView(name);
    }

    private void showView(String name) {
        Node view = views.get(name);
        if (view == null) {
            logger.warn("Unknown view: {}", name);
            return;
        }
        // Only the visible view's transport ticks and owns the play/pause visuals over the shared player.
        if (currentView instanceof AudioView hidden) hidden.onHidden();
        contentArea.getChildren().setAll(view);
        currentView = view;
        if (view instanceof AudioView shown) shown.onShown();
        refreshGlobalTimeline();
    }

    /**
     * Global transport keyboard shortcuts over the session's shared player: Space toggles play/pause,
     * ←/→ seek ∓1s (Shift for ∓5s), Home jumps to the start.
     *
     * <p>Only fires while a song is loaded, and yields the key to the focused control when it needs it
     * (text fields keep Space/Home for editing; sliders keep the arrows/Home for nudging) so the
     * shortcuts never fight normal input. Seeks go through {@link AppController#requestSeek} so every
     * view's transport (and the spine) stays in sync; play/pause targets the visible view so its
     * transport icon and timer track the change.
     */
    private void handleTransportKey(KeyEvent e) {
        if (!controller.audioPlayer().isLoaded()) return;
        Node focus = getScene().getFocusOwner();
        boolean typing = focus instanceof TextInputControl;
        switch (e.getCode()) {
            case SPACE -> {
                if (typing || focus instanceof ButtonBase) return; // let Space type / click the focused control
                if (currentView instanceof AudioView av) av.togglePlay();
                else togglePlayerDirect(); // no audio view visible — still toggle the shared player
                e.consume();
            }
            case LEFT -> {
                if (typing || focus instanceof Slider) return;
                seekBy(e.isShiftDown() ? -5 : -1);
                e.consume();
            }
            case RIGHT -> {
                if (typing || focus instanceof Slider) return;
                seekBy(e.isShiftDown() ? 5 : 1);
                e.consume();
            }
            case HOME -> {
                if (typing || focus instanceof Slider) return;
                controller.requestSeek(0);
                e.consume();
            }
            default -> { /* not a transport key */ }
        }
    }

    /** Seeks the shared player by {@code delta} seconds, clamped to the song, via the controller so all views sync. */
    private void seekBy(double delta) {
        AudioPreviewPlayer player = controller.audioPlayer();
        double target = Math.max(0, Math.min(player.durationSeconds(), player.positionSeconds() + delta));
        controller.requestSeek(target);
    }

    /** Toggles the shared player directly (used when no audio view is visible to own the play/pause visuals). */
    private void togglePlayerDirect() {
        AudioPreviewPlayer player = controller.audioPlayer();
        if (player.isPlaying()) player.pause();
        else player.play();
    }

    /** Releases resources held by views (e.g. the local map zip server, audio lines) on app shutdown. */
    public void shutdown() {
        spineClock.stop();
        views.values().forEach(view -> {
            if (view instanceof UserInterfaceFX.Views.ReviewView reviewView) reviewView.shutdown();
            if (view instanceof UserInterfaceFX.Views.TimingView timingView) timingView.shutdown();
            if (view instanceof UserInterfaceFX.Views.ViewerView viewerView) viewerView.shutdown();
        });
        controller.audioPlayer().close(); // the session owns the shared player, so the shell closes it
    }
}