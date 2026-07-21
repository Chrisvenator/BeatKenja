package UserInterfaceFX;

import MapAnalysation.PatternVisualisation.NpsPlotters.NpsInfo;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.List;

/**
 * NPS-over-time chart for the NPS overview screen, drawn on two stacked canvases:
 * the base canvas holds the data (bookmark lane, area/line plot, density strip, axis)
 * and is repainted only on data change or resize; the overlay canvas holds the hover
 * crosshair and is repainted on every mouse move.
 *
 * Emphasis form: the active diff is drawn as accent-colored area fill, the other
 * visible diffs as thin gray context lines. A dashed line marks the average NPS.
 * Vertical lanes top to bottom: bookmarks, plot, density strip, time axis (mm:ss).
 */
public class NpsTimelineCanvas extends Region {

    public record DiffSeries(String diffName, List<NpsInfo> samples, boolean active, Color identityColor) {}

    public record BookmarkMarker(float timeSeconds, String name) {}

    private static final double BOOKMARK_LANE = 20;
    private static final double DENSITY_LANE = 14;
    private static final double AXIS_LANE = 20;
    private static final double LANE_GAP = 4;
    private static final double PAD_LEFT = 8;
    private static final double PAD_RIGHT = 8;
    private static final Font LABEL_FONT = Font.font(11);

    private final Canvas base = new Canvas();
    private final Canvas overlay = new Canvas();
    private final VBox tooltip = new VBox(2);
    private final NpsChartPalette palette = NpsChartPalette.current();

    private List<DiffSeries> series = List.of();
    private float averageNps;
    private List<BookmarkMarker> bookmarks = List.of();
    private int[] densityBins = new int[0];
    private float densityBinSeconds = 1f;
    private float mapLengthSeconds;

    public NpsTimelineCanvas() {
        tooltip.setVisible(false);
        tooltip.setMouseTransparent(true);
        tooltip.setPadding(new Insets(6, 10, 6, 10));
        tooltip.setStyle("-fx-background-color: -color-bg-overlay; -fx-background-radius: 6;"
                + " -fx-border-color: -color-border-default; -fx-border-radius: 6; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0, 0, 2);");
        getChildren().addAll(base, overlay, tooltip);

        overlay.setOnMouseMoved(e -> drawHover(e.getX(), e.getY()));
        overlay.setOnMouseExited(e -> clearHover());
        setMinHeight(220);
    }

    public void setData(List<DiffSeries> visibleSeries, float averageNps, List<BookmarkMarker> bookmarks,
                        int[] densityBins, float densityBinSeconds, float mapLengthSeconds) {
        this.series = visibleSeries == null ? List.of() : visibleSeries;
        this.averageNps = averageNps;
        this.bookmarks = bookmarks == null ? List.of() : bookmarks;
        this.densityBins = densityBins == null ? new int[0] : densityBins;
        this.densityBinSeconds = densityBinSeconds;
        this.mapLengthSeconds = mapLengthSeconds;
        clearHover();
        paintBase();
    }

    public void clear() {
        setData(List.of(), 0, List.of(), new int[0], 1f, 0);
    }

    public static String formatMmSs(float seconds) {
        int total = Math.max(0, Math.round(seconds));
        return String.format("%d:%02d", total / 60, total % 60);
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        if (base.getWidth() != w || base.getHeight() != h) {
            base.setWidth(w);
            base.setHeight(h);
            overlay.setWidth(w);
            overlay.setHeight(h);
            paintBase();
        }
    }

    //<editor-fold desc="Geometry">
    private double plotTop() {
        return BOOKMARK_LANE + LANE_GAP;
    }

    private double plotBottom() {
        return getHeight() - AXIS_LANE - DENSITY_LANE - 2 * LANE_GAP;
    }

    private double densityTop() {
        return plotBottom() + LANE_GAP;
    }

    private double xOf(float timeSeconds) {
        return PAD_LEFT + (timeSeconds / mapLengthSeconds) * (getWidth() - PAD_LEFT - PAD_RIGHT);
    }

    private float timeOf(double x) {
        return (float) ((x - PAD_LEFT) / (getWidth() - PAD_LEFT - PAD_RIGHT) * mapLengthSeconds);
    }

    private double yOf(float nps, float maxNps) {
        double top = plotTop();
        return plotBottom() - (nps / maxNps) * (plotBottom() - top);
    }

    /** Max NPS over all visible series (and the average line), padded so peaks don't touch the lane above. */
    private float maxNps() {
        float max = averageNps;
        for (DiffSeries s : series)
            for (NpsInfo info : s.samples()) max = Math.max(max, info.nps());
        return max <= 0 ? 1 : max * 1.08f;
    }

    private static float sampleMidTime(NpsInfo info) {
        return Math.max(0, (info.fromTime() + info.toTime()) / 2);
    }
    //</editor-fold>

    //<editor-fold desc="Base layer">
    private void paintBase() {
        GraphicsContext g = base.getGraphicsContext2D();
        double w = base.getWidth();
        double h = base.getHeight();
        g.clearRect(0, 0, w, h);
        if (w <= 0 || h <= 0) return;

        if (series.isEmpty() || mapLengthSeconds <= 0) {
            g.setFill(palette.mutedText());
            g.setFont(LABEL_FONT);
            g.setTextAlign(TextAlignment.LEFT);
            g.fillText("No notes to plot — load a map under 1 · Load", 20, 30);
            return;
        }

        float maxNps = maxNps();
        paintGridAndYLabels(g, maxNps);
        paintContextSeries(g, maxNps);
        paintActiveSeries(g, maxNps);
        paintAverageLine(g, maxNps);
        paintBookmarks(g);
        paintDensityStrip(g);
        paintXAxis(g);
    }

    private void paintGridAndYLabels(GraphicsContext g, float maxNps) {
        double yStep = niceStep(maxNps, new double[]{0.5, 1, 2, 5, 10, 20}, 5);
        g.setFont(LABEL_FONT);
        g.setTextAlign(TextAlignment.LEFT);
        for (double nps = 0; nps <= maxNps; nps += yStep) {
            double y = yOf((float) nps, maxNps);
            g.setStroke(palette.gridline());
            g.setLineWidth(1);
            g.strokeLine(PAD_LEFT, y, getWidth() - PAD_RIGHT, y);
            if (nps > 0) {
                g.setFill(palette.mutedText());
                g.fillText(trimNumber(nps), PAD_LEFT + 4, y - 3);
            }
        }
    }

    private void paintContextSeries(GraphicsContext g, float maxNps) {
        g.setLineWidth(1.5);
        g.setStroke(palette.contextGray());
        for (DiffSeries s : series) {
            if (s.active()) continue;
            strokeSeries(g, s, maxNps);
        }
    }

    private void paintActiveSeries(GraphicsContext g, float maxNps) {
        for (DiffSeries s : series) {
            if (!s.active()) continue;

            // Area fill: series polyline closed down to the baseline
            List<NpsInfo> samples = s.samples();
            if (samples.isEmpty()) continue;
            double[] xs = new double[samples.size() + 2];
            double[] ys = new double[samples.size() + 2];
            for (int i = 0; i < samples.size(); i++) {
                xs[i + 1] = xOf(sampleMidTime(samples.get(i)));
                ys[i + 1] = yOf(samples.get(i).nps(), maxNps);
            }
            xs[0] = xs[1];
            ys[0] = plotBottom();
            xs[xs.length - 1] = xs[xs.length - 2];
            ys[ys.length - 1] = plotBottom();
            g.setFill(palette.accentFill());
            g.fillPolygon(xs, ys, xs.length);

            g.setLineWidth(2);
            g.setStroke(palette.accent());
            strokeSeries(g, s, maxNps);
        }
    }

    private void strokeSeries(GraphicsContext g, DiffSeries s, float maxNps) {
        List<NpsInfo> samples = s.samples();
        g.beginPath();
        for (int i = 0; i < samples.size(); i++) {
            double x = xOf(sampleMidTime(samples.get(i)));
            double y = yOf(samples.get(i).nps(), maxNps);
            if (i == 0) g.moveTo(x, y);
            else g.lineTo(x, y);
        }
        g.stroke();
    }

    private void paintAverageLine(GraphicsContext g, float maxNps) {
        if (averageNps <= 0) return;
        double y = yOf(averageNps, maxNps);
        g.setStroke(palette.avgLine());
        g.setLineWidth(1);
        g.setLineDashes(6, 4);
        g.strokeLine(PAD_LEFT, y, getWidth() - PAD_RIGHT, y);
        g.setLineDashes((double[]) null);
        g.setFill(palette.mutedText());
        g.setTextAlign(TextAlignment.RIGHT);
        g.fillText("avg " + trimNumber(averageNps), getWidth() - PAD_RIGHT - 2, y - 4);
        g.setTextAlign(TextAlignment.LEFT);
    }

    /** Bookmark ticks with labels; labels are skipped when they would collide with the previous one. */
    private void paintBookmarks(GraphicsContext g) {
        g.setFont(LABEL_FONT);
        double lastLabelEnd = -Double.MAX_VALUE;
        for (BookmarkMarker marker : bookmarks) {
            if (marker.timeSeconds() < 0 || marker.timeSeconds() > mapLengthSeconds) continue;
            double x = xOf(marker.timeSeconds());
            g.setStroke(palette.mutedText());
            g.setLineWidth(1);
            g.strokeLine(x, BOOKMARK_LANE - 6, x, BOOKMARK_LANE);
            if (x > lastLabelEnd + 6) {
                String label = marker.name() == null ? "" : marker.name();
                if (label.length() > 14) label = label.substring(0, 13) + "…";
                g.setFill(palette.mutedText());
                g.fillText(label, x + 2, BOOKMARK_LANE - 8);
                lastLabelEnd = x + label.length() * 6.0;
            }
        }
    }

    private void paintDensityStrip(GraphicsContext g) {
        if (densityBins.length == 0) return;
        int max = 0;
        for (int bin : densityBins) max = Math.max(max, bin);
        if (max == 0) return;

        double top = densityTop();
        for (int i = 0; i < densityBins.length; i++) {
            double x0 = xOf(i * densityBinSeconds);
            double x1 = xOf(Math.min((i + 1) * densityBinSeconds, mapLengthSeconds));
            g.setFill(palette.density((double) densityBins[i] / max));
            g.fillRect(x0, top, Math.max(1, x1 - x0), DENSITY_LANE);
        }
    }

    private void paintXAxis(GraphicsContext g) {
        double step = niceStep(mapLengthSeconds, new double[]{5, 10, 15, 30, 60, 120}, 8);
        double y = getHeight() - AXIS_LANE + 4;
        g.setFont(LABEL_FONT);
        g.setTextAlign(TextAlignment.CENTER);
        g.setFill(palette.mutedText());
        for (double t = 0; t <= mapLengthSeconds; t += step) {
            g.fillText(formatMmSs((float) t), xOf((float) t), y + 10);
        }
        g.setTextAlign(TextAlignment.LEFT);
    }

    /** Picks the smallest step from the candidates that yields at most targetTicks ticks. */
    private static double niceStep(double range, double[] candidates, int targetTicks) {
        for (double candidate : candidates) {
            if (range / candidate <= targetTicks) return candidate;
        }
        return candidates[candidates.length - 1] * Math.ceil(range / (candidates[candidates.length - 1] * targetTicks));
    }

    private static String trimNumber(double value) {
        return value == Math.floor(value) ? String.valueOf((int) value) : String.format("%.1f", value);
    }
    //</editor-fold>

    //<editor-fold desc="Hover layer">
    private void drawHover(double mouseX, double mouseY) {
        GraphicsContext g = overlay.getGraphicsContext2D();
        g.clearRect(0, 0, overlay.getWidth(), overlay.getHeight());
        if (series.isEmpty() || mapLengthSeconds <= 0
                || mouseX < PAD_LEFT || mouseX > getWidth() - PAD_RIGHT
                || mouseY < plotTop() || mouseY > densityTop() + DENSITY_LANE) {
            tooltip.setVisible(false);
            return;
        }

        float time = timeOf(mouseX);
        g.setStroke(palette.crosshair());
        g.setLineWidth(1);
        g.strokeLine(mouseX, plotTop(), mouseX, densityTop() + DENSITY_LANE);

        tooltip.getChildren().clear();
        Label header = new Label(formatMmSs(time));
        header.getStyleClass().addAll(atlantafx.base.theme.Styles.TEXT_SMALL, atlantafx.base.theme.Styles.TEXT_BOLD);
        tooltip.getChildren().add(header);
        for (DiffSeries s : series) {
            Float nps = npsAt(s, time);
            if (nps == null) continue;
            Label row = new Label(s.diffName().replace(".dat", "") + "  " + String.format("%.1f", nps));
            row.getStyleClass().add(atlantafx.base.theme.Styles.TEXT_SMALL);
            row.setGraphic(new Circle(4, s.identityColor()));
            tooltip.getChildren().add(row);
        }

        tooltip.setVisible(true);
        tooltip.applyCss();
        tooltip.autosize();
        double tx = mouseX + 14 + tooltip.getWidth() > getWidth() ? mouseX - tooltip.getWidth() - 14 : mouseX + 14;
        double ty = Math.min(mouseY, getHeight() - tooltip.getHeight() - 8);
        tooltip.relocate(tx, ty);
    }

    /** Nearest sample of the series at the given time; null when the series has no samples. */
    private Float npsAt(DiffSeries s, float time) {
        List<NpsInfo> samples = s.samples();
        if (samples.isEmpty()) return null;
        Float best = null;
        float bestDist = Float.MAX_VALUE;
        for (NpsInfo info : samples) {
            float dist = Math.abs(sampleMidTime(info) - time);
            if (dist < bestDist) {
                bestDist = dist;
                best = info.nps();
            }
        }
        return best;
    }

    private void clearHover() {
        overlay.getGraphicsContext2D().clearRect(0, 0, overlay.getWidth(), overlay.getHeight());
        tooltip.setVisible(false);
    }
    //</editor-fold>
}
