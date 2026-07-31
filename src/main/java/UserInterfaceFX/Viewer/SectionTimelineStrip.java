package UserInterfaceFX.Viewer;

import AppLogic.SectionAnalysisService.SectionAnalysis;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * 2D horizontal timeline showing section bands, onset ticks, boundary lines and a playhead.
 * Click-to-seek via {@link #setOnSeek}.
 * <p>
 * Uses the same holder-Pane resize idiom as TimingView's Song Map canvas: the holder is
 * pinned to prefWidth=0 so the bound canvas width never feeds back into the parent's
 * preferred size (avoids the "grows one pixel per layout pass" trap).
 */
public final class SectionTimelineStrip extends Pane {

    private static final double HEIGHT = 48;

    private final Canvas canvas = new Canvas(0, HEIGHT);
    private SectionAnalysis analysis;
    private double playheadSeconds = -1;
    private Runnable seekCallback;  // populated via setOnSeek

    public SectionTimelineStrip() {
        // Canvas must never drive the pane's pref size — pin pref width to 0
        setPrefSize(0, HEIGHT);
        setMinSize(0, HEIGHT);
        setMaxHeight(HEIGHT);
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        canvas.widthProperty().addListener((obs, o, n) -> draw());
        getChildren().add(canvas);

        canvas.setOnMouseClicked(e -> {
            if (analysis == null || canvas.getWidth() <= 0) return;
            double frac = e.getX() / canvas.getWidth();
            double seekSec = frac * analysis.durationSeconds();
            playheadSeconds = seekSec;
            draw();
            if (seekCallback != null) seekCallback.run();
            // caller reads back position via lastClickedSeconds()
            lastClickedSecondsInternal = seekSec;
        });
    }

    // Simple way to pass the seek time back to the caller after a click
    private double lastClickedSecondsInternal = 0;

    public double lastClickedSeconds() {
        return lastClickedSecondsInternal;
    }

    /**
     * @param cb called when the user clicks the strip; caller reads {@link #lastClickedSeconds()}
     */
    public void setOnSeek(Runnable cb) {
        this.seekCallback = cb;
    }

    public void setAnalysis(SectionAnalysis analysis) {
        this.analysis = analysis;
        this.playheadSeconds = -1;
        draw();
    }

    public void setPlayheadSeconds(double seconds) {
        this.playheadSeconds = seconds;
        draw();
    }

    public void clear() {
        this.analysis = null;
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

        // Section bands
        for (int s = 0; s < analysis.tiers().length; s++) {
            double start = s == 0 ? 0 : analysis.boundaries().get(s - 1);
            double end = s == analysis.tiers().length - 1
                    ? analysis.durationSeconds() : analysis.boundaries().get(s);
            float[] c = getColor(analysis.tiers()[s]);
            g.setFill(Color.color(c[0], c[1], c[2], 0.5));
            g.fillRect(start * pps, 0, (end - start) * pps, h);
        }

        // Onset ticks in the bottom 20%
        g.setStroke(Color.color(1, 1, 1, 0.3));
        g.setLineWidth(1);
        for (double t : analysis.onsetTimesSeconds()) {
            double x = t * pps;
            g.strokeLine(x, h * 0.8, x, h);
        }

        // Boundary lines
        g.setStroke(Color.color(0, 0, 0, 0.6));
        g.setLineWidth(1.5);
        for (double b : analysis.boundaries()) {
            double x = b * pps;
            g.strokeLine(x, 0, x, h);
        }

        // Playhead
        if (playheadSeconds >= 0) {
            g.setStroke(Color.color(1.0, 0.84, 0.25, 0.95));
            g.setLineWidth(2);
            double x = playheadSeconds * pps;
            g.strokeLine(x, 0, x, h);
        }
    }

    private static float[] getColor(int tier) {
        float[][] colors = AppLogic.SectionAnalysisService.TIER_COLORS;
        if (tier < 0 || tier >= colors.length) return new float[]{0.5f, 0.5f, 0.5f};
        return colors[tier];
    }
}
