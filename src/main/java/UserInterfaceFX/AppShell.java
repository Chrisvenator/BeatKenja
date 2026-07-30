package UserInterfaceFX;

import AppLogic.AppController;
import AppLogic.AppState;
import UserInterfaceFX.Views.LoadView;
import UserInterfaceFX.Views.SettingsView;
import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
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
    private final HBox diffChips = new HBox(6);

    /** Workflow steps that need a loaded map before they make sense. */
    private static final String[] LOCKED_STEPS = {"2 · Timing", "3 · Generate", "4 · Review", "5 · Export", "NPS Overview", "Characteristics"};

    public AppShell(AppController controller, Stage stage) {
        this.controller = controller;
        FxLog.install();

        registerViews(stage);
        setTop(buildToolBar());
        setLeft(buildSidebar());
        setCenter(buildCenter());
        setBottom(buildStatusBar());

        wireControllerEvents();
        // Dev aid: -Dbk.view=<name> opens a specific view on startup (smoke screenshots)
        selectView(System.getProperty("bk.view", "1 · Load"));
    }

    private void registerViews(Stage stage) {
        views.put("1 · Load", new LoadView(controller, stage, this::refreshMapHeader, this::selectView));
        views.put("2 · Timing", new UserInterfaceFX.Views.TimingView(controller));
        views.put("3 · Generate", new UserInterfaceFX.Views.GenerateView(controller, stage));
        views.put("4 · Review", new UserInterfaceFX.Views.ReviewView(controller, this::selectView));
        views.put("5 · Export", new UserInterfaceFX.Views.ExportView(controller, stage));
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

        Button settings = new Button("⚙ Settings");
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

        box.getChildren().add(new Separator());
        box.getChildren().add(sectionLabel("TOOLS"));
        for (String name : new String[]{"NPS Overview", "Utilities", "Characteristics", "Batch MP3", "Patterns"}) {
            box.getChildren().add(navButton(name));
        }

        // Steps 2-5 unlock once a map is loaded
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

        HBox header = new HBox(16, mapHeaderTitle, diffChips);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 16, 12, 16));

        contentArea.setPadding(new Insets(0, 16, 16, 16));
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        return new VBox(header, new Separator(), contentArea);
    }

    private VBox buildStatusBar() {
        ListView<String> logView = new ListView<>(FxLog.lines());
        logView.setPrefHeight(160);
        logView.setVisible(false);
        logView.setManaged(false);

        ToggleButton logToggle = new ToggleButton("Log ▴");
        logToggle.getStyleClass().add(Styles.FLAT);
        logToggle.setOnAction(e -> {
            boolean show = logToggle.isSelected();
            logView.setVisible(show);
            logView.setManaged(show);
            logToggle.setText(show ? "Log ▾" : "Log ▴");
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
                    refreshMapHeader();
                });
            }

            @Override
            public void onBpmChanged(double bpm) {
                Platform.runLater(AppShell.this::refreshMapHeader);
            }

            @Override
            public void onActiveDiffChanged(AppLogic.DiffSession activeDiff) {
                Platform.runLater(AppShell.this::refreshMapHeader);
            }
        });
    }

    /** Updates the map header (folder name + diff chips) from the controller session. */
    private void refreshMapHeader() {
        String folder = controller.session().getMapFolderPath();
        if (folder == null || controller.maps().isEmpty()) {
            mapHeaderTitle.setText("No map loaded");
            diffChips.getChildren().clear();
            return;
        }

        String songName = folder.replace('\\', '/');
        songName = songName.substring(songName.lastIndexOf('/') + 1);
        mapHeaderTitle.setText(songName + "  ·  BPM " + controller.session().getBpm());

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

            Button deleteBtn = new Button("✕");
            deleteBtn.getStyleClass().addAll(Styles.FLAT, Styles.DANGER, Styles.SMALL);
            deleteBtn.setTooltip(new javafx.scene.control.Tooltip("Remove this diff from the session (does not delete the file)"));
            deleteBtn.setOnAction(e -> controller.unloadDiff(diff.difficultyFileName()));

            // Merge chip label and delete button visually into one pill
            HBox chipRow = new HBox(0, chip, deleteBtn);
            chipRow.setAlignment(Pos.CENTER_LEFT);
            chipRow.setStyle("-fx-border-color: -color-accent-emphasis; -fx-border-radius: 4; -fx-background-radius: 4;");
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
        contentArea.getChildren().setAll(view);
    }

    /** Releases resources held by views (e.g. the local map zip server, audio lines) on app shutdown. */
    public void shutdown() {
        views.values().forEach(view -> {
            if (view instanceof UserInterfaceFX.Views.ReviewView reviewView) reviewView.shutdown();
            if (view instanceof UserInterfaceFX.Views.TimingView timingView) timingView.shutdown();
        });
    }
}