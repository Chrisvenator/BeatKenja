package UserInterfaceFX;

import DataManager.Parameters;
import javafx.scene.paint.Color;

/**
 * Color set for the NPS overview canvas (Canvas gets no CSS, so the AtlantaFX
 * Primer theme colors are mirrored here as constants). Two hand-picked sets for
 * light and dark mode; the identity colors were checked with a CVD/contrast
 * palette validator against both surfaces.
 *
 * The theme is fixed at startup ({@code Parameters.DARK_MODE}), so {@link #current()}
 * can be called once per component. If runtime theme switching is ever added,
 * call it in the paint path instead (cheap).
 */
public record NpsChartPalette(
        Color surface,
        Color gridline,
        Color axisText,
        Color mutedText,
        Color accent,
        Color accentFill,
        Color contextGray,
        Color avgLine,
        Color crosshair,
        Color densityLow,
        Color densityHigh) {

    private static final NpsChartPalette LIGHT = new NpsChartPalette(
            Color.web("#FFFFFF"),
            Color.web("#D0D7DE"),
            Color.web("#1F2328"),
            Color.web("#656D76"),
            Color.web("#0969DA"),
            Color.web("#0969DA", 0.18),
            Color.web("#8C959F"),
            Color.web("#656D76"),
            Color.web("#1F2328", 0.6),
            Color.web("#FFFFFF"),
            Color.web("#0969DA"));

    private static final NpsChartPalette DARK = new NpsChartPalette(
            Color.web("#0D1117"),
            Color.web("#30363D"),
            Color.web("#E6EDF3"),
            Color.web("#7D8590"),
            Color.web("#4493F8"),
            Color.web("#4493F8", 0.22),
            Color.web("#768390"),
            Color.web("#7D8590"),
            Color.web("#E6EDF3", 0.6),
            Color.web("#0D1117"),
            Color.web("#58A6FF"));

    public static NpsChartPalette current() {
        return Parameters.DARK_MODE ? DARK : LIGHT;
    }

    /**
     * Fixed Beat Saber difficulty identity color (Easy green … ExpertPlus purple),
     * keyed on the difficulty file name prefix. Identity only — used for chip dots
     * and tooltip markers, never for series lines (the chart uses emphasis form).
     */
    public static Color identityColor(String diffFileName) {
        boolean dark = Parameters.DARK_MODE;
        String name = diffFileName == null ? "" : diffFileName.toLowerCase();
        if (name.startsWith("expertplus")) return Color.web(dark ? "#A371F7" : "#8250DF");
        if (name.startsWith("expert")) return Color.web(dark ? "#F85149" : "#CF222E");
        if (name.startsWith("hard")) return Color.web(dark ? "#DB6D28" : "#BC4C00");
        if (name.startsWith("normal")) return Color.web(dark ? "#4493F8" : "#0969DA");
        if (name.startsWith("easy")) return Color.web(dark ? "#2EA043" : "#1A7F37");
        return Color.web(dark ? "#768390" : "#8C959F");
    }

    /** Sequential density ramp: surface → accent hue, linear in t (0..1). */
    public Color density(double t) {
        return densityLow.interpolate(densityHigh, Math.max(0, Math.min(1, t)));
    }
}
