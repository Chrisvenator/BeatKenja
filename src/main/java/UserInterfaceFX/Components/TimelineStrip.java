package UserInterfaceFX.Components;

import AppLogic.SectionAnalysisService;
import AppLogic.SectionAnalysisService.SectionAnalysis;
import BeatSaberObjects.Objects.Bookmark;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;

/**
 * Shared horizontal song timeline: the app's signature visual. Renders the section
 * intensity heat-ribbon (calm → peak, {@link SectionAnalysisService#TIER_COLORS}),
 * onset ticks, boundary lines, inline bookmark markers, a playhead, and an optional
 * structure-change novelty curve. Clicking the strip seeks (fires {@link #setOnSeek}).
 *
 * <p>One component now backs both the Timing "Song Map" card (tall, novelty on) and the
 * 3D Viewer strip (short, novelty off); height and lanes are configured per host. Bookmark
 * markers convert their beat time to seconds via the supplied BPM, so applied section
 * bookmarks show up as colored flags over the very bands they came from.
 *
 * <p>Uses the holder-Pane resize idiom: this Pane pins its preferred width to 0 so the bound
 * canvas width never feeds back into an ancestor's preferred size (which would grow the window
 * a pixel per layout pass).
 */
public final class TimelineStrip extends Pane {

    private final double height;
    private final Canvas canvas;

    private SectionAnalysis analysis;
    private List<Bookmark> bookmarks = List.of();
    private double bookmarkBpm;
    private double playheadSeconds = -1;
    private boolean showNovelty;
    private DoubleConsumer onSeek = seconds -> { };

    public TimelineStrip(double height) {
        this.height = height;
        this.canvas = new Canvas(0, height);

        // Canvas must never drive this pane's pref size — pin pref width to 0 (see class Javadoc).
        setPrefSize(0, height);
        setMinSize(0, height);
        setMaxHeight(height);
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        canvas.widthProperty().addListener((obs, o, n) -> draw());
        getChildren().add(canvas);

        canvas.setOnMouseClicked(e -> {
            if (analysis == null || canvas.getWidth() <= 0 || analysis.durationSeconds() <= 0) return;
            double seconds = e.getX() / canvas.getWidth() * analysis.durationSeconds();
            playheadSeconds = seconds;
            draw();
            onSeek.accept(seconds);
        });
    }

    /** Registers the click-to-seek callback (fired with the clicked position in seconds). */
    public void setOnSeek(DoubleConsumer callback) {
        this.onSeek = callback == null ? seconds -> { } : callback;
    }

    /** Sets the analysis to render (sections, onsets, novelty) and resets the playhead. */
    public void setAnalysis(SectionAnalysis analysis) {
        this.analysis = analysis;
        this.playheadSeconds = -1;
        draw();
    }

    /** Sets the bookmarks to mark; {@code bpm} converts their beat times to seconds. */
    public void setBookmarks(List<Bookmark> bookmarks, double bpm) {
        this.bookmarks = bookmarks == null ? List.of() : new ArrayList<>(bookmarks);
        this.bookmarkBpm = bpm;
        draw();
    }

    /** Toggles the novelty curve (on for the Timing Song Map, off for the compact Viewer strip). */
    public void setShowNovelty(boolean show) {
        this.showNovelty = show;
        draw();
    }

    public void setPlayheadSeconds(double seconds) {
        this.playheadSeconds = seconds;
        draw();
    }

    public void clear() {
        this.analysis = null;
        this.bookmarks = List.of();
        this.playheadSeconds = -1;
        draw();
    }

    private void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        g.clearRect(0, 0, w, h);
        if (analysis == null || w <= 0 || analysis.durationSeconds() <= 0) return;

        double pps = w / analysis.durationSeconds(); // pixels per second

        // Section bands, colored by intensity tier — the heat-ribbon.
        for (int s = 0; s < analysis.tiers().length; s++) {
            double start = s == 0 ? 0 : analysis.boundaries().get(s - 1);
            double end = s == analysis.tiers().length - 1
                    ? analysis.durationSeconds() : analysis.boundaries().get(s);
            float[] c = tierColor(analysis.tiers()[s]);
            g.setFill(Color.color(c[0], c[1], c[2], 0.35));
            g.fillRect(start * pps, 0, (end - start) * pps, h);
        }

        // Onset ticks in the bottom strip.
        g.setStroke(Color.color(1, 1, 1, 0.25));
        g.setLineWidth(1);
        for (double t : analysis.onsetTimesSeconds()) {
            double x = t * pps;
            g.strokeLine(x, h * 0.82, x, h);
        }

        // Novelty curve (scaled to its own max so shape stays readable in the upper band).
        if (showNovelty && analysis.novelty().length > 0) {
            double maxNovelty = 1e-9;
            for (double v : analysis.novelty()) maxNovelty = Math.max(maxNovelty, v);
            g.setStroke(Color.color(1, 1, 1, 0.9));
            g.setLineWidth(1.5);
            g.beginPath();
            for (int i = 0; i < analysis.novelty().length; i++) {
                double x = analysis.noveltyTimesSeconds()[i] * pps;
                double y = h * 0.78 * (1 - analysis.novelty()[i] / maxNovelty) + h * 0.02;
                if (i == 0) g.moveTo(x, y);
                else g.lineTo(x, y);
            }
            g.stroke();
        }

        // Boundary lines.
        g.setStroke(Color.color(0, 0, 0, 0.6));
        g.setLineWidth(1.5);
        for (double b : analysis.boundaries()) {
            double x = b * pps;
            g.strokeLine(x, 0, x, h);
        }

        // Inline bookmark markers: a colored flag at the top edge + a faint full-height line.
        if (bookmarkBpm > 0) {
            for (Bookmark bookmark : bookmarks) {
                double seconds = bookmark._time / bookmarkBpm * 60.0;
                double x = seconds * pps;
                Color color = bookmarkColor(bookmark);
                g.setStroke(color.deriveColor(0, 1, 1, 0.4));
                g.setLineWidth(1);
                g.strokeLine(x, 0, x, h);
                g.setFill(color);
                g.fillPolygon(new double[]{x - 4, x + 4, x}, new double[]{0, 0, 7}, 3);
            }
        }

        // Playhead.
        if (playheadSeconds >= 0) {
            g.setStroke(Color.color(1.0, 0.84, 0.25, 0.95));
            g.setLineWidth(2);
            double x = playheadSeconds * pps;
            g.strokeLine(x, 0, x, h);
        }
    }

    private static float[] tierColor(int tier) {
        float[][] colors = SectionAnalysisService.TIER_COLORS;
        if (tier < 0 || tier >= colors.length) return new float[]{0.5f, 0.5f, 0.5f};
        return colors[tier];
    }

    private static Color bookmarkColor(Bookmark bookmark) {
        float[] c = bookmark.getColor();
        if (c == null || c.length < 3) return Color.color(0.95, 0.95, 0.95, 0.95);
        return Color.color(clamp(c[0]), clamp(c[1]), clamp(c[2]), 0.95);
    }

    private static double clamp(float channel) {
        return Math.min(1, Math.max(0, channel));
    }
}
