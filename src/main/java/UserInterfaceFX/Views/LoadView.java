package UserInterfaceFX.Views;

import AppLogic.AppController;
import AppLogic.AppState;
import AppLogic.DiffSession;
import DataManager.Parameters;
import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import static DataManager.Parameters.logger;

/**
 * Step 1: load a single difficulty (.dat) or a whole map folder.
 * Supports file/folder buttons and drag &amp; drop; delegates the actual loading
 * to the AppController.
 *
 * After a successful load a "next steps" panel explains how to continue and
 * offers direct navigation to Generate (or Timing conversion first). The view
 * rebuilds itself from the controller state, so unloading or autoloading
 * elsewhere keeps it consistent.
 */
public class LoadView extends VBox {

    private final AppController controller;
    private final Runnable onLoaded;
    private final Consumer<String> navigate;
    private final Label dropLabel = new Label("Drop a .dat difficulty or a map folder here");
    private final Label errorLabel = new Label();
    private final ListView<String> loadedList = new ListView<>();
    private final VBox nextSteps = new VBox(10);

    public LoadView(AppController controller, Stage stage, Runnable onLoaded, Consumer<String> navigate) {
        super(16);
        this.controller = controller;
        this.onLoaded = onLoaded;
        this.navigate = navigate;
        setPadding(new Insets(16));

        getChildren().addAll(buildDropZone(stage), buildNextSteps(), buildLoadedList());
        refreshFromState();

        controller.addListener(new AppController.Listener() {
            @Override
            public void onStateChanged(AppState state) {
                Platform.runLater(LoadView.this::refreshFromState);
            }
        });
    }

    private VBox buildDropZone(Stage stage) {
        dropLabel.getStyleClass().add(Styles.TITLE_4);
        errorLabel.getStyleClass().add(Styles.DANGER);

        Button openFile = new Button("Open difficulty…");
        openFile.getStyleClass().add(Styles.ACCENT);
        openFile.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose a difficulty file");
            chooser.setInitialDirectory(existingDir(Parameters.filePath));
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Beat Saber difficulty (*.dat)", "*.dat"));
            File file = chooser.showOpenDialog(stage);
            if (file != null) load(file);
        });

        Button openFolder = new Button("Open map folder…");
        openFolder.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choose a map folder");
            chooser.setInitialDirectory(existingDir(Parameters.filePath));
            File dir = chooser.showDialog(stage);
            if (dir != null) load(dir);
        });

        HBox buttons = new HBox(12, openFile, openFolder);
        buttons.setAlignment(Pos.CENTER);

        VBox dropZone = new VBox(12, dropLabel, buttons, errorLabel);
        dropZone.setAlignment(Pos.CENTER);
        dropZone.setPadding(new Insets(32));
        dropZone.getStyleClass().add("bk-drop-zone");
        dropZone.setOnDragOver(this::acceptFiles);
        dropZone.setOnDragDropped(e -> {
            Dragboard board = e.getDragboard();
            if (board.hasFiles()) load(board.getFiles().get(0));
            e.setDropCompleted(true);
            e.consume();
        });
        return dropZone;
    }

    /** Panel shown after a successful load: what happened + where to go next. */
    private VBox buildNextSteps() {
        Label whatNow = new Label("What now?");
        whatNow.getStyleClass().add(Styles.TITLE_4);

        Label explanation = new Label("""
                • Generate needs timing notes (1-color dot notes). If your diff already is a timing diff, go straight to Generate.
                • If you loaded a normal map, convert it under 2 · Timing first.
                • The generated map only lives in memory until you save it under 5 · Export.""");
        explanation.getStyleClass().add(Styles.TEXT_MUTED);
        explanation.setWrapText(true);
        explanation.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

        Button toGenerate = new Button("Continue → 3 · Generate");
        toGenerate.getStyleClass().add(Styles.ACCENT);
        toGenerate.setOnAction(e -> navigate.accept("3 · Generate"));

        Button toTiming = new Button("Convert timings first → 2 · Timing");
        toTiming.setOnAction(e -> navigate.accept("2 · Timing"));

        HBox actions = new HBox(12, toGenerate, toTiming);

        nextSteps.getChildren().setAll(whatNow, explanation, actions);
        nextSteps.setPadding(new Insets(12, 16, 12, 16));
        nextSteps.getStyleClass().add("bk-info-card");
        return nextSteps;
    }

    private VBox buildLoadedList() {
        Label loadedLabel = new Label("Loaded difficulties");
        loadedLabel.getStyleClass().add(Styles.TEXT_MUTED);

        Button unloadAll = new Button("🗑 Unload all");
        unloadAll.getStyleClass().addAll(Styles.FLAT, Styles.DANGER, Styles.SMALL);
        unloadAll.setOnAction(e -> controller.unload());

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox listHeader = new HBox(8, loadedLabel, spacer, unloadAll);
        listHeader.setAlignment(Pos.CENTER_LEFT);

        loadedList.setCellFactory(list -> new DiffCell());
        VBox.setVgrow(loadedList, Priority.ALWAYS);
        VBox box = new VBox(6, listHeader, loadedList);
        VBox.setVgrow(box, Priority.ALWAYS);
        return box;
    }

    /** List row: difficulty name on the left, trash button on the right to unload just that diff. */
    private class DiffCell extends javafx.scene.control.ListCell<String> {
        private final Label name = new Label();
        private final HBox row;

        DiffCell() {
            Button trash = new Button("🗑");
            trash.getStyleClass().addAll(Styles.FLAT, Styles.DANGER, Styles.SMALL);
            trash.setTooltip(new javafx.scene.control.Tooltip("Unload this difficulty"));
            trash.setOnAction(e -> controller.unloadDiff(getItem()));

            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            row = new HBox(8, name, spacer, trash);
            row.setAlignment(Pos.CENTER_LEFT);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                name.setText(item);
                setGraphic(row);
            }
        }
    }

    /** Syncs drop label, next-steps panel, and diff list with the controller session. */
    private void refreshFromState() {
        boolean loaded = !controller.maps().isEmpty();
        nextSteps.setVisible(loaded);
        nextSteps.setManaged(loaded);

        if (loaded) {
            loadedList.getItems().setAll(controller.session().diffs().stream().map(DiffSession::difficultyFileName).toList());
            dropLabel.setText("✓ " + loadedList.getItems().size() + " difficulty file(s) loaded — drop a .dat to add, drop a folder to replace all");
        } else {
            loadedList.getItems().clear();
            dropLabel.setText("Drop a .dat difficulty or a map folder here");
        }
    }

    private void acceptFiles(DragEvent e) {
        if (e.getDragboard().hasFiles()) e.acceptTransferModes(TransferMode.COPY);
        e.consume();
    }

    /**
     * Loads a file or folder. Single .dat files are added to the existing session
     * additively; a collision (same filename already loaded) prompts the user to
     * replace or cancel. Folder loads always replace the full session.
     */
    private void load(File target) {
        errorLabel.setText("");
        try {
            boolean isSingleDiff = target.isFile() && target.getName().toLowerCase().endsWith(".dat")
                    && !target.getName().equalsIgnoreCase("info.dat");

            if (isSingleDiff && !controller.maps().isEmpty()) {
                // Reject additive loads from a different folder — mixing map folders corrupts session state
                // (export path, BPM extraction, etc. all assume one folder per session).
                String sessionFolder = controller.session().getMapFolderPath();
                if (sessionFolder != null && !target.getParentFile().getAbsolutePath().equals(new java.io.File(sessionFolder).getAbsolutePath())) {
                    errorLabel.setText("Cannot mix diffs from different folders. Drop the folder to replace the session.");
                    return;
                }

                // Additive path: try to add without overwrite first
                boolean added = controller.addDiffToSession(target, false);
                if (!added) {
                    // Collision — ask user
                    ButtonType replaceBtn = new ButtonType("Replace", ButtonBar.ButtonData.OK_DONE);
                    ButtonType cancelBtn  = new ButtonType("Cancel",  ButtonBar.ButtonData.CANCEL_CLOSE);
                    Alert confirm = new Alert(Alert.AlertType.WARNING);
                    confirm.setTitle("Diff already loaded");
                    confirm.setHeaderText("\"" + target.getName() + "\" is already loaded.");
                    confirm.setContentText("Replace the loaded diff with the file on disk, or cancel?");
                    confirm.getButtonTypes().setAll(replaceBtn, cancelBtn);
                    Optional<ButtonType> choice = confirm.showAndWait();
                    if (choice.isEmpty() || choice.get() == cancelBtn) {
                        errorLabel.setText("Cancelled — existing diff kept.");
                        return;
                    }
                    controller.addDiffToSession(target, true);
                }
            } else {
                controller.loadMapFileOrFolder(target);
            }
            Parameters.filePath = controller.session().getMapFolderPath();
            onLoaded.run();
        } catch (Exception ex) {
            logger.error("Error while loading map: {}", ex.getMessage());
            errorLabel.setText("Load failed: " + ex.getMessage());
        }
    }

    private File existingDir(String path) {
        File dir = new File(path == null ? "." : path);
        return dir.isDirectory() ? dir : new File(".");
    }
}