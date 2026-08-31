package UserInterfaceFX.Views;

import AppLogic.AppController;
import AppLogic.AppState;
import AppLogic.DiffSession;
import BeatSaberObjects.Objects.BeatSaberMap;
import MapAnalysation.PatternVisualisation.NpsPlotters.NpsInfo;
import MapAnalysation.PatternVisualisation.NpsPlotters.NpsSeriesBuilder;
import UserInterfaceFX.NpsChartPalette;
import UserInterfaceFX.NpsTimelineCanvas;
import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NPS overview dashboard: KPI tiles for the active diff, an NPS-over-time chart in
 * emphasis form (active diff filled, other visible diffs as gray context lines) with
 * average line, bookmark markers, note-density strip and hover crosshair.
 *
 * Diff chips toggle visibility only — the active diff is switched via the map header
 * and is always visible. The window selector changes the sliding-window width of the
 * NPS computation (constant 0.5 s sample step, presets 1/2/5 s). Computed series are
 * cached per diff and invalidated when the map reference, BPM or window changes.
 */
public class NpsOverviewView extends VBox {

    /** Sliding-window sample step in seconds; the presets only change the window width. */
    private static final float SAMPLE_STEP_SECONDS = 0.5f;
    private static final float DENSITY_BIN_SECONDS = 1f;

    private record CachedSeries(BeatSaberMap map, double bpm, int windowSeconds,
                                List<NpsInfo> samples, NpsSeriesBuilder.NpsKpis kpis, int[] densityBins) {}

    private final AppController controller;
    private final NpsTimelineCanvas chart = new NpsTimelineCanvas();
    private final Map<String, CachedSeries> cache = new HashMap<>();
    /** Diff names the user toggled off; survives chip rebuilds on reload. */
    private final Set<String> hiddenDiffs = new HashSet<>();

    private final Label avgValue = kpiValue();
    private final Label peakValue = kpiValue();
    private final Label peakCaption = kpiCaption("");
    private final Label notesValue = kpiValue();
    private final Label lengthValue = kpiValue();
    private final HBox diffChips = new HBox(6);
    private final ComboBox<String> windowSelector = new ComboBox<>();

    public NpsOverviewView(AppController controller) {
        super(12);
        this.controller = controller;
        setPadding(new Insets(16));

        HBox kpiRow = new HBox(12,
                kpiTile("Average NPS", avgValue, kpiCaption("over the whole map")),
                kpiTile("Peak NPS", peakValue, peakCaption),
                kpiTile("Notes", notesValue, kpiCaption("active diff")),
                kpiTile("Length", lengthValue, kpiCaption("to last note")));

        windowSelector.getItems().addAll("1 s", "2 s", "5 s");
        windowSelector.getSelectionModel().select("2 s");
        windowSelector.setTooltip(new javafx.scene.control.Tooltip(
                "Width of the sliding window used to average NPS (notes per second) — wider smooths the curve."));
        windowSelector.setOnAction(e -> {
            cache.clear();
            refresh();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox controls = new HBox(8, new Label("Diffs:"), diffChips, spacer, new Label("Window:"), windowSelector);
        controls.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(chart, Priority.ALWAYS);
        getChildren().addAll(kpiRow, controls, chart);
        refresh();

        controller.addListener(new AppController.Listener() {
            @Override
            public void onStateChanged(AppState state) {
                Platform.runLater(NpsOverviewView.this::refresh);
            }

            @Override
            public void onBpmChanged(double bpm) {
                Platform.runLater(NpsOverviewView.this::refresh);
            }

            @Override
            public void onActiveDiffChanged(DiffSession activeDiff) {
                Platform.runLater(NpsOverviewView.this::refresh);
            }
        });
    }

    //<editor-fold desc="KPI tiles">
    private static Label kpiValue() {
        Label label = new Label("–");
        label.getStyleClass().add(Styles.TITLE_2);
        return label;
    }

    private static Label kpiCaption(String text) {
        Label label = new Label(text);
        label.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED);
        return label;
    }

    private static VBox kpiTile(String name, Label value, Label caption) {
        Label title = new Label(name);
        title.getStyleClass().addAll(Styles.TEXT_SMALL, Styles.TEXT_MUTED);
        VBox tile = new VBox(2, title, value, caption);
        tile.getStyleClass().add("bk-kpi-tile");
        HBox.setHgrow(tile, Priority.ALWAYS);
        tile.setMaxWidth(Double.MAX_VALUE);
        return tile;
    }
    //</editor-fold>

    private int windowSeconds() {
        return Integer.parseInt(windowSelector.getValue().replace(" s", ""));
    }

    private void refresh() {
        DiffSession active = controller.getActiveDiff();
        rebuildDiffChips(active);

        if (active == null || active.map()._notes == null || active.map()._notes.length == 0) {
            avgValue.setText("–");
            peakValue.setText("–");
            peakCaption.setText("");
            notesValue.setText("–");
            lengthValue.setText("–");
            chart.clear();
            return;
        }

        double bpm = controller.session().getBpm();
        CachedSeries activeData = dataFor(active, bpm);
        avgValue.setText(String.format("%.2f", activeData.kpis().averageNps()));
        peakValue.setText(String.format("%.1f", activeData.kpis().peakNps()));
        peakCaption.setText("at " + NpsTimelineCanvas.formatMmSs(activeData.kpis().peakTimeSeconds()));
        notesValue.setText(String.valueOf(activeData.kpis().noteCount()));
        lengthValue.setText(NpsTimelineCanvas.formatMmSs(activeData.kpis().mapLengthSeconds()));

        // Context series first so the active (emphasized) one paints on top
        List<NpsTimelineCanvas.DiffSeries> visible = new ArrayList<>();
        for (DiffSession diff : controller.session().diffs()) {
            if (diff == active || hiddenDiffs.contains(diff.difficultyFileName())) continue;
            if (diff.map()._notes == null || diff.map()._notes.length == 0) continue;
            visible.add(new NpsTimelineCanvas.DiffSeries(diff.difficultyFileName(), dataFor(diff, bpm).samples(),
                    false, NpsChartPalette.identityColor(diff.difficultyFileName())));
        }
        visible.add(new NpsTimelineCanvas.DiffSeries(active.difficultyFileName(), activeData.samples(),
                true, NpsChartPalette.identityColor(active.difficultyFileName())));

        chart.setData(visible, activeData.kpis().averageNps(), bookmarkMarkers(active, bpm),
                activeData.densityBins(), DENSITY_BIN_SECONDS, activeData.kpis().mapLengthSeconds());
    }

    /** Returns the cached series for the diff, recomputing when map, BPM or window changed. */
    private CachedSeries dataFor(DiffSession diff, double bpm) {
        int window = windowSeconds();
        CachedSeries cached = cache.get(diff.difficultyFileName());
        if (cached != null && cached.map() == diff.map() && cached.bpm() == bpm && cached.windowSeconds() == window) {
            return cached;
        }

        List<NpsInfo> samples = NpsSeriesBuilder.computeNpsSeconds(diff.map()._notes, bpm, SAMPLE_STEP_SECONDS, window);
        NpsSeriesBuilder.NpsKpis kpis = NpsSeriesBuilder.computeKpis(diff.map()._notes, bpm, samples);
        int[] density = NpsSeriesBuilder.computeDensityBins(diff.map()._notes, bpm, DENSITY_BIN_SECONDS);
        CachedSeries fresh = new CachedSeries(diff.map(), bpm, window, samples, kpis, density);
        cache.put(diff.difficultyFileName(), fresh);
        return fresh;
    }

    private List<NpsTimelineCanvas.BookmarkMarker> bookmarkMarkers(DiffSession active, double bpm) {
        List<NpsTimelineCanvas.BookmarkMarker> markers = new ArrayList<>();
        if (active.map().bookmarks == null || bpm <= 0) return markers;
        active.map().bookmarks.forEach(b ->
                markers.add(new NpsTimelineCanvas.BookmarkMarker((float) (b._time / bpm * 60), b._name)));
        return markers;
    }

    /** Visibility chips: dot in the diff's identity color; the active diff can't be hidden. */
    private void rebuildDiffChips(DiffSession active) {
        diffChips.getChildren().clear();
        for (DiffSession diff : controller.session().diffs()) {
            String name = diff.difficultyFileName();
            ToggleButton chip = new ToggleButton(name.replace(".dat", ""));
            chip.getStyleClass().add(Styles.TEXT_SMALL);
            chip.setGraphic(new Circle(4, NpsChartPalette.identityColor(name)));
            boolean isActive = diff == active;
            chip.setTooltip(new javafx.scene.control.Tooltip(isActive
                    ? "The active diff is always shown in the NPS chart."
                    : "Show or hide this difficulty in the NPS chart."));
            chip.setSelected(isActive || !hiddenDiffs.contains(name));
            chip.setDisable(isActive);
            chip.setOnAction(e -> {
                if (chip.isSelected()) hiddenDiffs.remove(name);
                else hiddenDiffs.add(name);
                refresh();
            });
            diffChips.getChildren().add(chip);
        }
    }
}
