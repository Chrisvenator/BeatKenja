package UserInterfaceFX.Views;

import DataManager.Config.Configuration;
import DataManager.Parameters;
import atlantafx.base.theme.Styles;
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
        save.setOnAction(e -> save());

        saveResult.getStyleClass().add(Styles.TEXT_MUTED);
        VBox footer = new VBox(8, save, saveResult);
        footer.setPadding(new Insets(8, 0, 0, 0));
        grid.add(footer, 0, row, 2, 1);

        loadCurrentValues();
        setContent(grid);
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
            saveResult.setText("Saved. Most settings take effect after a restart.");
        } catch (NumberFormatException ex) {
            saveResult.setText("Default BPM and Style drift must be numbers.");
        } catch (Exception ex) {
            logger.error("Could not save config: {}", ex.getMessage());
            saveResult.setText("Save failed: " + ex.getMessage());
        }
    }
}