package UserInterfaceFX.Views;

import AppLogic.AppController;
import AppLogic.AppState;
import AppLogic.DiffSession;
import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

import static DataManager.Parameters.logger;

/**
 * Step 5: write the generated result to disk. Two modes:
 *
 * "Save difficulties" writes only the selected .dat files — one save dialog for a single
 * diff, a folder dialog when several are selected. "Save as map" packs the whole map
 * (folder contents + in-memory diffs) into one zip via a save dialog. Both default to
 * the folder the map was loaded from. Backup of overwritten diff files is ON by default.
 */
public class ExportView extends VBox {

    /** One table row per loaded diff. */
    public static class ExportRow {
        final DiffSession diff;
        final SimpleBooleanProperty selected = new SimpleBooleanProperty(true);
        final SimpleStringProperty status = new SimpleStringProperty("");

        ExportRow(DiffSession diff) {
            this.diff = diff;
        }
    }

    private final AppController controller;
    private final Stage stage;
    private final ObservableList<ExportRow> rows = FXCollections.observableArrayList();
    private final CheckBox backup = new CheckBox("Create backup of overwritten difficulty files (recommended)");
    private final Label result = new Label();

    public ExportView(AppController controller, Stage stage) {
        super(12);
        this.controller = controller;
        this.stage = stage;
        setPadding(new Insets(16));

        Label hint = new Label("Generated maps only exist in memory until you save them. "
                + "\"Save difficulties\" writes just the selected .dat files; \"Save as map\" packs the whole map (song, cover, info.dat + current diffs) into one zip.");
        hint.getStyleClass().add(Styles.TEXT_MUTED);
        hint.setWrapText(true);
        hint.setMinHeight(Region.USE_PREF_SIZE);

        TableView<ExportRow> table = buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        backup.setSelected(true);
        backup.setTooltip(new Tooltip("Before overwriting a difficulty file, rename the old one to a numbered .bak backup."));

        Button saveDiffs = new Button("Save difficulties…");
        saveDiffs.getStyleClass().add(Styles.ACCENT);
        saveDiffs.setTooltip(new Tooltip("Writes the selected .dat files. One diff → file dialog; several → choose a target folder."));
        saveDiffs.setOnAction(e -> saveDifficulties(rows.filtered(r -> r.selected.get())));

        Button saveAsMap = new Button("Save as map (.zip)…");
        saveAsMap.setTooltip(new Tooltip("Zips the whole map folder with the current in-memory diffs patched in."));
        saveAsMap.setOnAction(e -> saveAsMap());

        Button previewer = new Button("Open in web previewer");
        previewer.getStyleClass().add(Styles.FLAT);
        previewer.setTooltip(new Tooltip("Zips the map folder and opens the previewer — drag the zip into the browser"));
        previewer.setOnAction(e -> openPreviewer());

        HBox actions = new HBox(12, saveDiffs, saveAsMap, previewer);
        actions.setAlignment(Pos.CENTER_LEFT);

        result.setWrapText(true);
        result.setMinHeight(Region.USE_PREF_SIZE);

        getChildren().addAll(hint, table, backup, actions, result);
        refreshRows();

        controller.addListener(new AppController.Listener() {
            @Override
            public void onStateChanged(AppState state) {
                Platform.runLater(ExportView.this::refreshRows);
            }
        });
    }

    private TableView<ExportRow> buildTable() {
        TableView<ExportRow> table = new TableView<>(rows);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<ExportRow, Boolean> selectCol = new TableColumn<>("");
        selectCol.setCellValueFactory(cell -> cell.getValue().selected);
        selectCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectCol));
        selectCol.setEditable(true);
        selectCol.setMaxWidth(40);

        TableColumn<ExportRow, String> diffCol = new TableColumn<>("Difficulty");
        diffCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().diff.difficultyFileName()));
        diffCol.setEditable(false);

        TableColumn<ExportRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cell -> cell.getValue().status);
        statusCol.setEditable(false);

        table.getColumns().setAll(List.of(selectCol, diffCol, statusCol));
        return table;
    }

    /** Single diff → file save dialog; several diffs → directory dialog, each saved under its own name. */
    private void saveDifficulties(List<ExportRow> targets) {
        if (targets.isEmpty()) {
            result.setText("No difficulties selected.");
            return;
        }

        File mapFolder = defaultFolder();
        if (targets.size() == 1) {
            ExportRow row = targets.get(0);
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save difficulty");
            if (mapFolder != null) chooser.setInitialDirectory(mapFolder);
            chooser.setInitialFileName(row.diff.difficultyFileName());
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Beat Saber difficulty (*.dat)", "*.dat"));
            File target = chooser.showSaveDialog(stage);
            if (target == null) return;

            saveRows(List.of(row), target.getParentFile(), target.getName());
        } else {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choose target folder for " + targets.size() + " difficulties");
            if (mapFolder != null) chooser.setInitialDirectory(mapFolder);
            File targetDir = chooser.showDialog(stage);
            if (targetDir == null) return;

            saveRows(List.copyOf(targets), targetDir, null);
        }
    }

    /** Writes each row to targetDir; fileNameOverride is only used for the single-diff dialog case. */
    private void saveRows(List<ExportRow> targets, File targetDir, String fileNameOverride) {
        int saved = 0;
        for (ExportRow row : targets) {
            String fileName = fileNameOverride != null ? fileNameOverride : row.diff.difficultyFileName();
            boolean ok = controller.saveMap(row.diff.map(), new File(targetDir, fileName).getAbsolutePath(), backup.isSelected());
            row.status.set(ok ? "✓ saved" : "✗ failed");
            if (ok) saved++;
        }

        if (saved == targets.size()) {
            controller.markSaved();
            result.setText("✓ Saved " + saved + " difficulty file(s) to " + targetDir.getAbsolutePath());
            result.getStyleClass().remove(Styles.DANGER);
        } else {
            result.setText("Saved " + saved + "/" + targets.size() + " — check the status column and the log");
            result.getStyleClass().add(Styles.DANGER);
        }
    }

    /** Whole map as one zip: save dialog, defaulting to the map folder and its name. */
    private void saveAsMap() {
        File mapFolder = defaultFolder();
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save map as zip");
        if (mapFolder != null) {
            chooser.setInitialDirectory(mapFolder);
            chooser.setInitialFileName(mapFolder.getName() + ".zip");
        }
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Zip archive (*.zip)", "*.zip"));
        File target = chooser.showSaveDialog(stage);
        if (target == null) return;

        try {
            controller.exportMapAsZip(target);
            controller.markSaved();
            result.setText("✓ Map saved as " + target.getAbsolutePath());
            result.getStyleClass().remove(Styles.DANGER);
        } catch (Exception ex) {
            logger.error("Zip export failed: {}", ex.getMessage());
            result.setText("Zip export failed: " + ex.getMessage());
            result.getStyleClass().add(Styles.DANGER);
        }
    }

    private void openPreviewer() {
        try {
            controller.openMapInBrowserPreviewer();
            result.setText("Zip created in the map folder — drag it into the previewer tab that just opened.");
            result.getStyleClass().remove(Styles.DANGER);
        } catch (Exception ex) {
            logger.error("Could not open previewer: {}", ex.getMessage());
            result.setText("Previewer failed: " + ex.getMessage());
            result.getStyleClass().add(Styles.DANGER);
        }
    }

    private File defaultFolder() {
        String folder = controller.session().getMapFolderPath();
        if (folder == null) return null;
        File dir = new File(folder);
        return dir.isDirectory() ? dir : null;
    }

    /** Rebuilds the rows from the session (load/unload/generate all end up here via state events). */
    private void refreshRows() {
        rows.clear();
        controller.session().diffs().forEach(diff -> rows.add(new ExportRow(diff)));
    }
}