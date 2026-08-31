package UserInterfaceFX.Views;

import DataManager.Config.Configuration;
import DataManager.Parameters;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Styles;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import static DataManager.Parameters.logger;

/**
 * Editor for config.json. Shows the settings a mapper actually touches
 * (paths, dark mode, previewer, parity-bookmark flags) and writes them back
 * via the ConfigLoader. Most values are read once at startup, so a restart
 * hint is shown after saving.
 */
public class SettingsView extends ScrollPane {

    private final Configuration config = Parameters.configLoader.getConfig();
    private final Label saveResult = new Label();

    // Global
    private final CheckBox darkMode = new CheckBox("Dark mode");
    private final CheckBox verbose = new CheckBox("Verbose logging");
    private final CheckBox ignoreDds = new CheckBox("Ignore DDs by default");
    private final CheckBox autoloadPattern = new CheckBox("Autoload default pattern");
    private final CheckBox parityBookmarks = new CheckBox("Save parity errors as bookmarks");
    private final CheckBox parityBookmarksOverwrite = new CheckBox("...overwrite existing bookmarks");
    private final TextField previewerUrl = new TextField();
    private final TextField secondaryPreviewerUrl = new TextField();

    // Paths
    private final TextField wipFolder = new TextField();
    private final TextField patternFolder = new TextField();
    private final TextField defaultPattern = new TextField();

    // Map generator
    private final TextField defaultBpm = new TextField();
    private final CheckBox plotNps = new CheckBox("Plot NPS distribution on load");
    private final CheckBox fixInconsistentTimings = new CheckBox("Fix inconsistent timings in fast sections");
    private final TextField styleDriftMagnitude = new TextField();

    public SettingsView() {
        setFitToWidth(true);

        ignoreDds.setTooltip(new javafx.scene.control.Tooltip(
                "DD = double-directional: back-to-back notes needing the same swing direction.\n"
                        + "When on, generation allows them instead of avoiding them."));
        parityBookmarksOverwrite.setTooltip(new javafx.scene.control.Tooltip(
                "When saving parity errors as bookmarks, replace the diff's existing bookmarks instead of adding to them."));
        fixInconsistentTimings.setTooltip(new javafx.scene.control.Tooltip(
                "In fast bursts (above ~8 NPS, 4+ notes), snap notes to even spacing so a slightly-off onset doesn't make the stream stutter."));

        darkMode.setTooltip(new javafx.scene.control.Tooltip(
                "Switch between the dark and light theme (applies immediately)."));
        verbose.setTooltip(new javafx.scene.control.Tooltip(
                "Log extra detail for debugging."));
        autoloadPattern.setTooltip(new javafx.scene.control.Tooltip(
                "Load the default pattern automatically on startup."));
        parityBookmarks.setTooltip(new javafx.scene.control.Tooltip(
                "After a parity check, save each error as a colored bookmark in the diff (parity = good/bad hand swing resets)."));
        plotNps.setTooltip(new javafx.scene.control.Tooltip(
                "Show the notes-per-second chart automatically when a map loads."));
        previewerUrl.setTooltip(new javafx.scene.control.Tooltip(
                "URL of the web map previewer opened from the Review and Export tabs."));
        secondaryPreviewerUrl.setTooltip(new javafx.scene.control.Tooltip(
                "URL of an alternate map previewer."));
        wipFolder.setTooltip(new javafx.scene.control.Tooltip(
                "Folder scanned for work-in-progress maps."));
        patternFolder.setTooltip(new javafx.scene.control.Tooltip(
                "Folder holding your saved pattern (.pat) files."));
        defaultPattern.setTooltip(new javafx.scene.control.Tooltip(
                "Pattern file the generators fall back to when none is loaded."));
        defaultBpm.setTooltip(new javafx.scene.control.Tooltip(
                "BPM assumed for a map when its own BPM is unknown."));
        styleDriftMagnitude.setTooltip(new javafx.scene.control.Tooltip(
                "How far the generation style may wander per section (0 = stay consistent, 0.2 = adventurous)."));

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(16));

        int row = 0;
        row = section(grid, row, "General");
        grid.add(darkMode, 0, row++, 2, 1);
        grid.add(verbose, 0, row++, 2, 1);
        grid.add(ignoreDds, 0, row++, 2, 1);
        grid.add(autoloadPattern, 0, row++, 2, 1);
        grid.add(parityBookmarks, 0, row++, 2, 1);
        grid.add(parityBookmarksOverwrite, 0, row++, 2, 1);
        row = field(grid, row, "Map previewer URL", previewerUrl);
        row = field(grid, row, "Secondary previewer URL", secondaryPreviewerUrl);

        row = section(grid, row, "Paths");
        row = field(grid, row, "WIP folder", wipFolder);
        row = field(grid, row, "Pattern folder", patternFolder);
        row = field(grid, row, "Default pattern", defaultPattern);

        row = section(grid, row, "Map generator");
        row = field(grid, row, "Default BPM", defaultBpm);
        grid.add(plotNps, 0, row++, 2, 1);
        grid.add(fixInconsistentTimings, 0, row++, 2, 1);
        row = field(grid, row, "Style drift per section (0=consistent, 0.2=adventurous)", styleDriftMagnitude);

        Button save = new Button("Save settings");
        save.getStyleClass().add(Styles.ACCENT);
        save.setTooltip(new javafx.scene.control.Tooltip(
                "Write these settings to config.json (most take effect after a restart)."));
        save.setOnAction(e -> save());

        saveResult.getStyleClass().add(Styles.TEXT_MUTED);
        VBox footer = new VBox(8, save, saveResult);
        footer.setPadding(new Insets(8, 0, 0, 0));
        grid.add(footer, 0, row, 2, 1);

        loadCurrentValues();

        // Live theme switch (no restart) + monospace numeric fields
        darkMode.setOnAction(e -> applyThemeLive(darkMode.isSelected()));
        defaultBpm.getStyleClass().add("bk-numeric");
        styleDriftMagnitude.getStyleClass().add("bk-numeric");

        setContent(grid);
    }

    /**
     * Applies the light/dark theme immediately, without a restart.
     *
     * Swaps the global AtlantaFX user-agent stylesheet, updates the runtime DARK_MODE flag
     * (so canvas palettes redraw in the matching theme), and re-keys the saber-blue accent
     * style class on the scene root. app.css stays applied via the scene's own stylesheets.
     */
    private void applyThemeLive(boolean dark) {
        Application.setUserAgentStylesheet(dark
                ? new PrimerDark().getUserAgentStylesheet()
                : new PrimerLight().getUserAgentStylesheet());
        Parameters.DARK_MODE = dark;
        if (getScene() != null && getScene().getRoot() != null) {
            var root = getScene().getRoot();
            root.getStyleClass().removeAll("bk-theme-dark", "bk-theme-light");
            root.getStyleClass().add(dark ? "bk-theme-dark" : "bk-theme-light");
        }
    }

    private int section(GridPane grid, int row, String title) {
        Label label = new Label(title);
        label.getStyleClass().add(Styles.TITLE_4);
        label.setPadding(new Insets(row == 0 ? 0 : 16, 0, 4, 0));
        grid.add(label, 0, row, 2, 1);
        return row + 1;
    }

    private int field(GridPane grid, int row, String labelText, TextField field) {
        Label label = new Label(labelText);
        field.setPrefWidth(420);
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        return row + 1;
    }

    private void loadCurrentValues() {
        darkMode.setSelected(config.global.darkMode);
        verbose.setSelected(config.global.verbose);
        ignoreDds.setSelected(config.global.ignoreDds);
        autoloadPattern.setSelected(config.global.autoloadDefaultPattern);
        parityBookmarks.setSelected(config.global.saveParityErrorsAsBookmarks);
        parityBookmarksOverwrite.setSelected(config.global.saveParityErrorsAsBookmarksWillOverwriteBookmarks);
        previewerUrl.setText(config.global.defaultMapPreviewer);
        secondaryPreviewerUrl.setText(config.global.secondaryMapPreviewer);

        wipFolder.setText(config.defaultPath.wipFolder);
        patternFolder.setText(config.defaultPath.patternFolder);
        defaultPattern.setText(config.defaultPath.defaultPattern);

        defaultBpm.setText(String.valueOf(config.mapGenerator.defaultBpm));
        plotNps.setSelected(config.mapGenerator.plotNpsDistribution);
        fixInconsistentTimings.setSelected(config.mapGenerator.fixInconsistentTimings);
        styleDriftMagnitude.setText(String.valueOf(config.mapGenerator.styleDriftMagnitude));
    }

    private void save() {
        try {
            config.global.darkMode = darkMode.isSelected();
            config.global.verbose = verbose.isSelected();
            config.global.ignoreDds = ignoreDds.isSelected();
            config.global.autoloadDefaultPattern = autoloadPattern.isSelected();
            config.global.saveParityErrorsAsBookmarks = parityBookmarks.isSelected();
            config.global.saveParityErrorsAsBookmarksWillOverwriteBookmarks = parityBookmarksOverwrite.isSelected();
            config.global.defaultMapPreviewer = previewerUrl.getText().trim();
            config.global.secondaryMapPreviewer = secondaryPreviewerUrl.getText().trim();

            config.defaultPath.wipFolder = wipFolder.getText().trim();
            config.defaultPath.patternFolder = patternFolder.getText().trim();
            config.defaultPath.defaultPattern = defaultPattern.getText().trim();

            config.mapGenerator.defaultBpm = Integer.parseInt(defaultBpm.getText().trim());
            config.mapGenerator.plotNpsDistribution = plotNps.isSelected();
            config.mapGenerator.fixInconsistentTimings = fixInconsistentTimings.isSelected();
            config.mapGenerator.styleDriftMagnitude = Float.parseFloat(styleDriftMagnitude.getText().trim());
            AppLogic.GenerationContext.styleDriftMagnitude = config.mapGenerator.styleDriftMagnitude;

            Parameters.configLoader.saveConfig(Parameters.CONFIG_FILE_LOCATION);
            saveResult.setText("Saved. Theme applies immediately; other settings take effect after a restart.");
        } catch (NumberFormatException ex) {
            saveResult.setText("Default BPM and Style drift must be numbers.");
        } catch (Exception ex) {
            logger.error("Could not save config: {}", ex.getMessage());
            saveResult.setText("Save failed: " + ex.getMessage());
        }
    }
}