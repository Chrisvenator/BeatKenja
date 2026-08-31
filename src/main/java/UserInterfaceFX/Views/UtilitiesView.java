package UserInterfaceFX.Views;

import AppLogic.AppController;
import AppLogic.AppState;
import AppLogic.DiffSession;
import BeatSaberObjects.Objects.Enums.BeatmapCharacteristic;
import atlantafx.base.theme.Styles;
import java.util.Optional;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Map utilities: flashing-light removal and note placement snapping.
 * Each utility can run on the active diff or on all loaded diffs.
 *
 * Note: no-arrow and one-saber transforms have moved to the Characteristics tab.
 */
public class UtilitiesView extends javafx.scene.control.ScrollPane {

    private final AppController controller;
    private final VBox content = new VBox(16);
    private final Label activeDiffLabel = new Label();
    private final Label result = new Label();

    public UtilitiesView(AppController controller) {
        this.controller = controller;
        setFitToWidth(true);
        setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        content.setPadding(new Insets(16));
        setContent(content);

        Label title = new Label("Map utilities");
        title.getStyleClass().add(Styles.TITLE_3);
        activeDiffLabel.getStyleClass().add(Styles.TEXT_MUTED);

        VBox lights = utilityRow(
                "Convert flashing lights",
                "Replaces all flashing light events with regular \"on\" events.",
                null,
                targets -> {
                    controller.convertFlashingLights(targets);
                    result.setText("✓ Converted flashing lights in " + targets.size() + " diff(s)");
                });

        TextField precision = new TextField("16");
        precision.setPrefWidth(70);
        precision.setTooltip(new javafx.scene.control.Tooltip(
                "Beat fraction to snap notes to (e.g. 16 = snap to 1/16 of a beat)."));
        VBox placements = utilityRow(
                "Fix placements",
                "Snaps every note to 1/x of a beat (default 1/16).",
                new HBox(6, new Label("1 /"), precision) {{
                    setAlignment(Pos.CENTER_LEFT);
                }},
                targets -> {
                    try {
                        double denominator = Double.parseDouble(precision.getText().replaceAll("[^\\d.]", ""));
                        if (denominator <= 0) throw new NumberFormatException();
                        controller.fixPlacements(targets, denominator);
                        result.setText("✓ Snapped notes to 1/" + precision.getText() + " of a beat in " + targets.size() + " diff(s)");
                    } catch (NumberFormatException ex) {
                        result.setText("Invalid precision: " + precision.getText());
                    }
                });

        Label charTitle = new Label("Change characteristic");
        charTitle.getStyleClass().add(Styles.TITLE_4);

        Label charDesc = new Label("Changes the characteristic of the active diff. Copy keeps the original and creates a new diff; Rename replaces the original (notes unchanged in both cases).");
        charDesc.getStyleClass().add(Styles.TEXT_MUTED);
        charDesc.setWrapText(true);
        charDesc.setMinHeight(Region.USE_PREF_SIZE);

        ComboBox<BeatmapCharacteristic> selector = new ComboBox<>();
        selector.getItems().addAll(
                BeatmapCharacteristic.ONE_SABER, BeatmapCharacteristic.NO_ARROWS,
                BeatmapCharacteristic.DEGREE_360, BeatmapCharacteristic.DEGREE_90,
                BeatmapCharacteristic.LAWLESS, BeatmapCharacteristic.LIGHTSHOW,
                BeatmapCharacteristic.LEGACY);
        selector.setConverter(new StringConverter<>() {
            private final Map<BeatmapCharacteristic, String> LABELS = Map.of(
                    BeatmapCharacteristic.ONE_SABER, "One Saber",
                    BeatmapCharacteristic.NO_ARROWS, "No Arrows",
                    BeatmapCharacteristic.DEGREE_360, "360 Degree",
                    BeatmapCharacteristic.DEGREE_90, "90 Degree",
                    BeatmapCharacteristic.LAWLESS, "Lawless",
                    BeatmapCharacteristic.LIGHTSHOW, "Lightshow",
                    BeatmapCharacteristic.LEGACY, "Legacy");

            @Override
            public String toString(BeatmapCharacteristic c) {
                return c == null ? "" : LABELS.getOrDefault(c, c.infoName);
            }

            @Override
            public BeatmapCharacteristic fromString(String s) { return null; }
        });
        selector.getSelectionModel().selectFirst();
        selector.setTooltip(new javafx.scene.control.Tooltip(
                "Choose which characteristic to relabel the active diff as."));

        Button changeCharBtn = new Button("Change characteristic");
        changeCharBtn.setTooltip(new javafx.scene.control.Tooltip(
                "Relabels the active diff under another characteristic as a new diff (notes copied as-is, no transform).\n"
                        + "Use the Characteristics tab to also transform the notes."));
        changeCharBtn.setOnAction(e -> onChangeCharacteristic(selector.getValue()));

        HBox charControls = new HBox(8, selector, changeCharBtn);
        charControls.setAlignment(Pos.CENTER_LEFT);

        VBox characteristicCard = new VBox(8, charTitle, charDesc, charControls);
        characteristicCard.setPadding(new Insets(12));
        characteristicCard.getStyleClass().add("bk-card");

        result.setWrapText(true);
        result.setMinHeight(Region.USE_PREF_SIZE);

        content.getChildren().addAll(title, activeDiffLabel, lights, placements, characteristicCard, result);
        refresh();

        controller.addListener(new AppController.Listener() {
            @Override
            public void onStateChanged(AppState state) {
                Platform.runLater(UtilitiesView.this::refresh);
            }

            @Override
            public void onActiveDiffChanged(DiffSession activeDiff) {
                Platform.runLater(UtilitiesView.this::refresh);
            }
        });
    }

    /**
     * Asks the user whether to copy or rename the active diff to the given characteristic,
     * then dispatches to the appropriate controller method.
     *
     * Copy — creates a new diff with the new characteristic, keeps the original.
     * Rename — replaces the original diff with a new characteristic diff (same notes).
     */
    private void onChangeCharacteristic(BeatmapCharacteristic characteristic) {
        DiffSession active = controller.getActiveDiff();
        if (active == null) { result.setText("No active diff selected."); return; }
        if (characteristic == null) { result.setText("Select a characteristic first."); return; }

        ButtonType copyBtn   = new ButtonType("Copy",   ButtonBar.ButtonData.YES);
        ButtonType renameBtn = new ButtonType("Rename", ButtonBar.ButtonData.NO);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert choiceDialog = new Alert(Alert.AlertType.CONFIRMATION);
        choiceDialog.setTitle("Change characteristic");
        choiceDialog.setHeaderText("Change characteristic to " + characteristic.infoName);
        choiceDialog.setContentText(
                "Copy — keep the original diff and create a new " + characteristic.infoName + " diff.\n" +
                "Rename — replace the original diff with a " + characteristic.infoName + " diff (same notes)."
        );
        choiceDialog.getButtonTypes().setAll(copyBtn, renameBtn, cancelBtn);

        Optional<ButtonType> choice = choiceDialog.showAndWait();
        if (choice.isEmpty() || choice.get() == cancelBtn) {
            result.setText("Cancelled.");
            return;
        }

        if (choice.get() == renameBtn) {
            DiffSession created = controller.renameCharacteristic(active, characteristic);
            if (created != null)
                result.setText("✓ Renamed to " + created.difficultyFileName() + " (" + characteristic.infoName + ")");
            return;
        }

        // Copy path — keep existing collision-check logic
        DiffSession created = controller.changeCharacteristic(active, characteristic, false);
        if (created == null) {
            String existingName = BeatmapCharacteristic.baseDifficulty(active.difficultyFileName())
                    + characteristic.filenameSuffix + ".dat";
            Alert confirm = new Alert(Alert.AlertType.WARNING);
            confirm.setTitle("Diff already exists");
            confirm.setHeaderText("\"" + existingName + "\" already exists in this session.");
            confirm.setContentText("Overwrite it with a new " + characteristic.infoName
                    + " diff from \"" + active.difficultyFileName() + "\"?");
            ButtonType overwrite = new ButtonType("Overwrite", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancel    = new ButtonType("Cancel",    ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(overwrite, cancel);
            if (confirm.showAndWait().filter(b -> b == overwrite).isEmpty()) {
                result.setText("Cancelled — existing diff kept.");
                return;
            }
            created = controller.changeCharacteristic(active, characteristic, true);
        }
        if (created != null)
            result.setText("✓ Created " + created.difficultyFileName() + " (" + characteristic.infoName + ")");
    }

    /** One bordered utility row: title, description, optional parameter controls, run buttons. */
    private VBox utilityRow(String title, String description, HBox params, Consumer<List<DiffSession>> action) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add(Styles.TITLE_4);

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add(Styles.TEXT_MUTED);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMinHeight(Region.USE_PREF_SIZE);

        Button runActive = new Button("Apply to active diff");
        runActive.setTooltip(new javafx.scene.control.Tooltip("Run this utility on the active difficulty only."));
        runActive.setOnAction(e -> {
            if (controller.getActiveDiff() != null) action.accept(List.of(controller.getActiveDiff()));
        });

        Button runAll = new Button("Apply to all diffs");
        runAll.getStyleClass().add(Styles.FLAT);
        runAll.setTooltip(new javafx.scene.control.Tooltip("Run this utility on every loaded difficulty."));
        runAll.setOnAction(e -> {
            if (!controller.session().diffs().isEmpty()) action.accept(List.copyOf(controller.session().diffs()));
        });

        HBox controls = new HBox(8);
        controls.setAlignment(Pos.CENTER_LEFT);
        if (params != null) {
            params.setAlignment(Pos.CENTER_LEFT);
            controls.getChildren().add(params);
        }
        controls.getChildren().addAll(runActive, runAll);

        VBox box = new VBox(8, titleLabel, descriptionLabel, controls);
        box.setPadding(new Insets(12));
        box.getStyleClass().add("bk-card");
        return box;
    }

    private void refresh() {
        boolean loaded = controller.state() != AppState.EMPTY;
        DiffSession active = controller.getActiveDiff();
        activeDiffLabel.setText(!loaded
                ? "Load a map first (1 · Load) — the utilities work on the loaded diffs."
                : active == null ? "No diff selected." : "Active diff: " + active.difficultyFileName());
        content.getChildren().stream().filter(n -> n instanceof VBox).forEach(n -> n.setDisable(!loaded));
    }
}
