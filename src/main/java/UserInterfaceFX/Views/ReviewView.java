package UserInterfaceFX.Views;

import AppLogic.AppController;
import AppLogic.AppState;
import AppLogic.ArcViewerManager;
import AppLogic.DiffSession;
import AppLogic.MapZipServer;
import BeatSaberObjects.Objects.Enums.ParityErrorEnum;
import BeatSaberObjects.Objects.Note;
import MapAnalysation.PatternVisualisation.NpsPlotters.DynamicNpsPlotter;
import MapAnalysation.PatternVisualisation.NpsPlotters.NpsInfo;
import MapGeneration.PatternGeneration.CommonMethods.NpsBpmConverter;
import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static DataManager.Parameters.NPS_COMPUTATION__IGNORE_STACKS_AND_SLIDERS;
import static DataManager.Parameters.NPS_COMPUTATION__INTERVAL_SIZE;
import static DataManager.Parameters.NPS_COMPUTATION__RANGE_INTERVALS;
import static DataManager.Parameters.logger;

/**
 * Step 4: inspect the generated map before saving.
 * Tabs: parity warnings (with bookmark export), NPS-over-time chart, pattern heatmap,
 * and external web check tools (MapCheck / bs-parity) fed by a local zip server.
 * "Preview in ArcViewer" downloads the desktop previewer on first use and launches it.
 */
public class ReviewView extends VBox {

    /** Row model for the parity warnings table. */
    public static class ParityRow {
        final float beatValue;
        final SimpleStringProperty beat;
        final SimpleStringProperty error;
        /** Reviewed-and-accepted by the user: grayed out, sorted to the bottom, bookmark removed. */
        final javafx.beans.property.SimpleBooleanProperty completed = new javafx.beans.property.SimpleBooleanProperty(false);

        ParityRow(Pair<Float, ParityErrorEnum> pair) {
            this.beatValue = pair.getKey();
            this.beat = new SimpleStringProperty(String.valueOf(pair.getKey()));
            this.error = new SimpleStringProperty(pair.getValue().toString());
        }
    }

    private final AppController controller;
    private final MapZipServer zipServer = new MapZipServer();

    /**
     * Stops the local map zip server on app shutdown. Its dispatcher thread is
     * non-daemon and would otherwise keep the JVM alive after the window closes.
     */
    public void shutdown() {
        zipServer.stop();
    }

    private final Label activeDiffLabel = new Label();
    private final Label parityCount = new Label();
    private final ObservableList<ParityRow> parityRows = FXCollections.observableArrayList();
    private final LineChart<Number, Number> npsChart = buildNpsChart();
    private final Canvas heatmapCanvas = new Canvas(760, 560);
    private final Label result = new Label();

    public ReviewView(AppController controller) {
        super(12);
        this.controller = controller;
        setPadding(new Insets(16));

        activeDiffLabel.getStyleClass().add(Styles.TEXT_MUTED);

        Button arcViewer = new Button("Preview in ArcViewer");
        arcViewer.getStyleClass().add(Styles.ACCENT);
        arcViewer.setTooltip(new javafx.scene.control.Tooltip("3D preview. Downloads the ArcViewer desktop app (~100 MB) from GitHub on first use."));
        arcViewer.setOnAction(e -> previewInArcViewer());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox top = new HBox(12, activeDiffLabel, spacer, arcViewer);
        top.setAlignment(Pos.CENTER_LEFT);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
                new Tab("Parity warnings", buildParityTab()),
                new Tab("NPS", npsChart),
                new Tab("Pattern heatmap", buildHeatmapTab()),
                new Tab("External checks", buildExternalTab()));
        VBox.setVgrow(tabs, Priority.ALWAYS);

        // Dev aid: -Dbk.reviewtab=<index> preselects a review tab (smoke screenshots)
        String devTab = System.getProperty("bk.reviewtab");
        if (devTab != null) tabs.getSelectionModel().select(Integer.parseInt(devTab));

        result.setWrapText(true);
        result.setMinHeight(Region.USE_PREF_SIZE);

        getChildren().addAll(top, tabs, result);
        refresh();

        controller.addListener(new AppController.Listener() {
            @Override
            public void onStateChanged(AppState state) {
                Platform.runLater(ReviewView.this::refresh);
            }

            @Override
            public void onActiveDiffChanged(DiffSession activeDiff) {
                Platform.runLater(ReviewView.this::refresh);
            }
        });
    }

    //<editor-fold desc="Parity tab">
    private VBox buildParityTab() {
        TableView<ParityRow> table = new TableView<>(parityRows);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<ParityRow, Boolean> okCol = new TableColumn<>("OK");
        okCol.setCellValueFactory(cell -> cell.getValue().completed);
        okCol.setCellFactory(javafx.scene.control.cell.CheckBoxTableCell.forTableColumn(okCol));
        okCol.setEditable(true);
        okCol.setMaxWidth(50);

        TableColumn<ParityRow, String> beatCol = new TableColumn<>("Beat");
        beatCol.setCellValueFactory(cell -> cell.getValue().beat);
        beatCol.setMaxWidth(120);

        TableColumn<ParityRow, String> errorCol = new TableColumn<>("Warning");
        errorCol.setCellValueFactory(cell -> cell.getValue().error);

        table.getColumns().setAll(List.of(okCol, beatCol, errorCol));
        VBox.setVgrow(table, Priority.ALWAYS);

        // Completed rows: gray out, drop the matching bookmark, sink to the bottom
        table.setRowFactory(t -> new javafx.scene.control.TableRow<>() {
            @Override
            protected void updateItem(ParityRow item, boolean empty) {
                super.updateItem(item, empty);
                styleProperty().unbind();
                if (item == null || empty) {
                    setStyle("");
                } else {
                    styleProperty().bind(javafx.beans.binding.Bindings
                            .when(item.completed)
                            .then("-fx-opacity: 0.45;")
                            .otherwise(""));
                }
            }
        });

        Button toBookmarks = new Button("Save as bookmarks in the diff");
        if (DataManager.Parameters.SAVE_PARITY_ERRORS_AS_BOOKMARKS) {
            toBookmarks.setDisable(true);
            toBookmarks.setText("Already saved as bookmarks (see settings)");
        }
        toBookmarks.setTooltip(new javafx.scene.control.Tooltip("Adds the warnings as colored bookmarks so your editor jumps right to them"));
        toBookmarks.setOnAction(e -> {
            DiffSession active = controller.getActiveDiff();
            if (active == null) return;
            active.map().bookmarks.addAll(controller.parityErrorsAsBookmarks(active.difficultyFileName()));
            result.setText("✓ Bookmarks added — export the diff under 5 · Export to persist them");
        });

        HBox bar = new HBox(12, parityCount, new Region(), toBookmarks);
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(8, bar, table);
        box.setPadding(new Insets(8));
        return box;
    }
    //</editor-fold>

    //<editor-fold desc="NPS tab">
    private static LineChart<Number, Number> buildNpsChart() {
        NumberAxis x = new NumberAxis();
        x.setLabel("Time (s)");
        NumberAxis y = new NumberAxis();
        y.setLabel("NPS");
        LineChart<Number, Number> chart = new LineChart<>(x, y);
        chart.setCreateSymbols(false);
        chart.setAnimated(false);
        return chart;
    }

    /** Computes NPS on a seconds-converted copy of the notes (the map itself stays in beats). */
    private void refreshNpsChart() {
        npsChart.getData().clear();
        DiffSession active = controller.getActiveDiff();
        if (active == null || active.map()._notes == null || active.map()._notes.length == 0) return;

        List<Note> notes = new ArrayList<>(Arrays.asList(active.map()._notes));
        NpsBpmConverter.convertBeatsToSeconds(notes);
        try {
            List<NpsInfo> npsInfos = DynamicNpsPlotter.computeNps(notes, NPS_COMPUTATION__INTERVAL_SIZE, NPS_COMPUTATION__RANGE_INTERVALS, NPS_COMPUTATION__IGNORE_STACKS_AND_SLIDERS);
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(active.difficultyFileName());
            for (NpsInfo info : npsInfos) series.getData().add(new XYChart.Data<>((info.fromTime() + info.toTime()) / 2, info.nps()));
            npsChart.getData().add(series);
        } finally {
            NpsBpmConverter.convertSecondsToBeats(notes);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Heatmap tab">
    private ScrollPane buildHeatmapTab() {
        Button info = new Button("ⓘ");
        info.getStyleClass().add(Styles.FLAT);
        info.setTooltip(new javafx.scene.control.Tooltip("What am I looking at?"));
        info.setOnAction(e -> showHeatmapInfo());

        HBox header = new HBox(8,
                muted("Transition probabilities of the loaded pattern: row = current note, column = possible next note. Stronger blue = more likely."),
                info);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(8, header, heatmapCanvas);
        box.setPadding(new Insets(8));
        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        return scroll;
    }

    /** Placeholder info popup — proper explanation content comes in the Optimization step. */
    private void showHeatmapInfo() {
        VBox content = new VBox(12,
                muted("PLACEHOLDER — a real explanation of the heatmap and how the variance slider shapes it will land here."),
                muted("Example: a low-variance pattern concentrates probability on few transitions (repetitive but safe):"));

        File exampleImage = new File("assets/variance_low_variance.png");
        if (exampleImage.exists()) {
            javafx.scene.image.ImageView image = new javafx.scene.image.ImageView(new javafx.scene.image.Image(exampleImage.toURI().toString()));
            image.setFitWidth(600);
            image.setPreserveRatio(true);
            content.getChildren().add(image);
        } else {
            content.getChildren().add(muted("(assets/variance_low_variance.png not found — image shown when running from the repo)"));
        }
        content.setPadding(new Insets(16));

        javafx.stage.Stage popup = new javafx.stage.Stage();
        popup.setTitle("Pattern heatmap — info");
        popup.setScene(new javafx.scene.Scene(new ScrollPane(content), 680, 520));
        popup.show();
    }

    private void refreshHeatmap() {
        UserInterfaceFX.PatternHeatmap.draw(heatmapCanvas, controller.getPattern());
    }
    //</editor-fold>

    //<editor-fold desc="External checks tab">
    private VBox buildExternalTab() {
        TextField servedUrl = new TextField();
        servedUrl.setEditable(false);
        servedUrl.setPromptText("Click \"Serve current map\" to get a local URL for the check tools");
        HBox.setHgrow(servedUrl, Priority.ALWAYS);

        javafx.scene.web.WebView web = new javafx.scene.web.WebView();
        VBox.setVgrow(web, Priority.ALWAYS);

        Button serve = new Button("Serve current map");
        serve.setTooltip(new javafx.scene.control.Tooltip("Exports the map as zip and serves it locally so the tools can load it via URL"));
        serve.setOnAction(e -> serveCurrentMap(servedUrl));

        Button mapCheck = new Button("MapCheck");
        mapCheck.setOnAction(e -> {
            if (serveCurrentMap(servedUrl)) web.getEngine().load(zipServer.mapCheckUrl());
        });

        Button bsParity = new Button("bs-parity");
        bsParity.setOnAction(e -> {
            if (serveCurrentMap(servedUrl)) web.getEngine().load(zipServer.bsParityUrl());
        });

        Button browser = new Button("Open in browser instead");
        browser.getStyleClass().add(Styles.FLAT);
        browser.setTooltip(new javafx.scene.control.Tooltip("If a tool doesn't render in the embedded view, open the same local URL in your real browser"));
        browser.setOnAction(e -> {
            if (!serveCurrentMap(servedUrl)) return;
            String current = web.getEngine().getLocation();
            String target = current != null && current.startsWith("http") ? current : zipServer.mapCheckUrl();
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(target));
            } catch (Exception ex) {
                logger.error("Could not open browser: {}", ex.getMessage());
            }
        });

        HBox bar = new HBox(8, serve, servedUrl, mapCheck, bsParity, browser);
        bar.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(8, bar, web);
        box.setPadding(new Insets(8));
        return box;
    }

    /**
     * Exports the current map as zip and serves it via the local server, so the check
     * tools (proxied on the same origin) can load it. Returns false if nothing is loaded
     * or the export failed.
     */
    private boolean serveCurrentMap(TextField servedUrl) {
        if (controller.session().getMapFolderPath() == null) {
            servedUrl.setText("Load a map first");
            return false;
        }
        try {
            File zip = new File(controller.session().getMapFolderPath(), "beatkenja-check.zip");
            controller.exportMapAsZip(zip);
            servedUrl.setText(zipServer.serve(zip.toPath()));
            return true;
        } catch (Exception ex) {
            logger.error("Could not serve map: {}", ex.getMessage());
            servedUrl.setText("Failed: " + ex.getMessage());
            return false;
        }
    }
    //</editor-fold>

    /** Exports the map as zip and opens it in ArcViewer, installing ArcViewer on first use (with consent). */
    private void previewInArcViewer() {
        if (controller.session().getMapFolderPath() == null) return;

        if (!ArcViewerManager.isInstalled()) {
            Alert consent = new Alert(Alert.AlertType.CONFIRMATION,
                    "ArcViewer is not installed yet. Download the latest release (~100 MB) from GitHub into ./tools/ArcViewer?",
                    ButtonType.YES, ButtonType.NO);
            consent.setHeaderText("Download ArcViewer?");
            if (consent.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
        }

        result.setText("Preparing ArcViewer preview…");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                if (!ArcViewerManager.isInstalled()) {
                    ArcViewerManager.install(msg -> Platform.runLater(() -> result.setText(msg)));
                }
                File zip = new File(controller.session().getMapFolderPath(), "beatkenja-preview.zip");
                controller.exportMapAsZip(zip);
                ArcViewerManager.launch(zip);
                return null;
            }
        };
        task.setOnSucceeded(e -> result.setText("✓ ArcViewer launched — if the map didn't open automatically, drag " + "beatkenja-preview.zip into it"));
        task.setOnFailed(e -> {
            logger.error("ArcViewer preview failed", task.getException());
            result.setText("ArcViewer failed: " + task.getException().getMessage());
        });
        new Thread(task, "arcviewer").start();
    }

    private void refresh() {
        DiffSession active = controller.getActiveDiff();
        activeDiffLabel.setText(active == null ? "No diff selected." : "Reviewing: " + active.difficultyFileName());

        parityRows.clear();
        if (active != null) {
            active.parityErrors().forEach(pair -> {
                ParityRow row = new ParityRow(pair);
                row.completed.addListener((obs, was, done) -> onParityRowToggled(row, done));
                parityRows.add(row);
            });
            updateParityCount();
        } else {
            parityCount.setText("");
        }

        refreshNpsChart();
        refreshHeatmap();
    }

    /**
     * A warning was checked off (or unchecked): remove/keep its bookmark in the diff and
     * re-sort so completed entries sink to the bottom.
     */
    private void onParityRowToggled(ParityRow row, boolean done) {
        DiffSession active = controller.getActiveDiff();
        if (active != null && done && active.map().bookmarks != null) {
            active.map().bookmarks.removeIf(b -> b._time == row.beatValue && b._name.equals(row.error.get()));
        }
        FXCollections.sort(parityRows, java.util.Comparator
                .comparing((ParityRow r) -> r.completed.get())
                .thenComparing(r -> r.beatValue));
        updateParityCount();
    }

    private void updateParityCount() {
        long open = parityRows.stream().filter(r -> !r.completed.get()).count();
        parityCount.setText(open + " open warning(s)" + (parityRows.size() > (int) open ? " · " + (parityRows.size() - open) + " checked off" : ""));
        parityCount.getStyleClass().removeAll(Styles.SUCCESS, Styles.WARNING);
        parityCount.getStyleClass().add(open == 0 ? Styles.SUCCESS : Styles.WARNING);
    }

    private static Label muted(String text) {
        Label label = new Label(text);
        label.getStyleClass().add(Styles.TEXT_MUTED);
        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);
        return label;
    }
}