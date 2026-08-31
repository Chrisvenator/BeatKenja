package UserInterfaceFX.Views;

import AppLogic.OnsetGenerationService;
import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static DataManager.Parameters.ONSET_GENERATION_FOLDER_PATH_INPUT;
import static DataManager.Parameters.ONSET_GENERATION_FOLDER_PATH_OUTPUT;
import static DataManager.Parameters.logger;

/**
 * Batch "MP3 → timing maps": drop songs into the onset input folder, convert them all to
 * timing maps in one go. Wraps OnsetGenerationService in a background task with a
 * per-file progress list.
 */
public class BatchMp3View extends VBox {

    private final ObservableList<String> fileList = FXCollections.observableArrayList();
    /** file name → last reported status, insertion-ordered for stable list rendering */
    private final Map<String, String> fileStatus = new LinkedHashMap<>();
    private final Label status = new Label();
    private final ProgressBar progress = new ProgressBar();
    private final Button convert = new Button("Convert MP3s to timing maps");

    public BatchMp3View() {
        super(12);
        setPadding(new Insets(16));

        Label title = new Label("Batch MP3 → timing maps");
        title.getStyleClass().add(Styles.TITLE_3);

        Label description = new Label(
                "Analyzes every song in the input folder and creates a timing map per song (5 no-arrow diffs from subtle to sensitive onset detection). "
                        + "Needs ffmpeg. Expect false positives — always review the timings in an editor afterwards.");
        description.getStyleClass().add(Styles.TEXT_MUTED);
        description.setWrapText(true);
        description.setMinHeight(Region.USE_PREF_SIZE);

        Button openInput = new Button("Open input folder");
        openInput.setTooltip(new javafx.scene.control.Tooltip(
                "Open the input folder in the file explorer — drop your .mp3 files here."));
        openInput.setOnAction(e -> openFolder(ONSET_GENERATION_FOLDER_PATH_INPUT));
        Button openOutput = new Button("Open output folder");
        openOutput.setTooltip(new javafx.scene.control.Tooltip(
                "Open the output folder in the file explorer — the generated timing maps land here."));
        openOutput.setOnAction(e -> openFolder(ONSET_GENERATION_FOLDER_PATH_OUTPUT));
        Button refresh = new Button("↻ Refresh");
        refresh.getStyleClass().add(Styles.FLAT);
        refresh.setTooltip(new javafx.scene.control.Tooltip("Rescan the input folder for MP3 files"));
        refresh.setOnAction(e -> refreshFileList());

        HBox folderBar = new HBox(8,
                new Label("Input: " + ONSET_GENERATION_FOLDER_PATH_INPUT), openInput,
                new Label("Output: " + ONSET_GENERATION_FOLDER_PATH_OUTPUT), openOutput, refresh);
        folderBar.setAlignment(Pos.CENTER_LEFT);

        ListView<String> files = new ListView<>(fileList);
        VBox.setVgrow(files, Priority.ALWAYS);

        convert.getStyleClass().add(Styles.ACCENT);
        convert.setTooltip(new javafx.scene.control.Tooltip(
                "Analyze every MP3 in the input folder and write a 5-diff no-arrow timing map per song. Needs ffmpeg on PATH."));
        convert.setOnAction(e -> runConversion());

        progress.setVisible(false);
        progress.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(progress, Priority.ALWAYS);
        HBox runBar = new HBox(12, convert, progress);
        runBar.setAlignment(Pos.CENTER_LEFT);

        status.setWrapText(true);
        status.setMinHeight(Region.USE_PREF_SIZE);

        getChildren().addAll(title, description, folderBar, files, runBar, status);
        refreshFileList();
    }

    private void refreshFileList() {
        fileStatus.clear();
        OnsetGenerationService.listConvertibleFiles().forEach(f -> fileStatus.put(f.getName(), ""));
        renderFileList();
        status.setText(fileStatus.isEmpty()
                ? "No songs found — put .mp3 files into the input folder, then hit Refresh."
                : fileStatus.size() + " file(s) found.");
    }

    private void renderFileList() {
        fileList.setAll(fileStatus.entrySet().stream()
                .map(e -> e.getValue().isEmpty() ? e.getKey() : e.getKey() + "   —   " + e.getValue())
                .toList());
    }

    private void runConversion() {
        convert.setDisable(true);
        progress.setVisible(true);
        progress.setProgress(-1);
        status.setText("Converting…");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                OnsetGenerationService.convertMp3FolderToTimingMaps((file, message) -> Platform.runLater(() -> {
                    if (file.isEmpty()) {
                        status.setText(message);
                    } else {
                        fileStatus.put(file, message);
                        renderFileList();
                    }
                }));
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            convert.setDisable(false);
            progress.setVisible(false);
        });
        task.setOnFailed(e -> {
            convert.setDisable(false);
            progress.setVisible(false);
            logger.error("MP3 conversion failed", task.getException());
            status.setText(task.getException().getMessage());
        });
        new Thread(task, "mp3-to-timings").start();
    }

    private void openFolder(String path) {
        try {
            File folder = new File(path);
            folder.mkdirs();
            java.awt.Desktop.getDesktop().open(folder);
        } catch (Exception ex) {
            logger.error("Could not open folder {}: {}", path, ex.getMessage());
            status.setText("Could not open folder: " + ex.getMessage());
        }
    }
}
