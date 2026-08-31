package UserInterfaceFX.Views;

import AppLogic.AppController;
import AppLogic.AppState;
import AppLogic.DiffSession;
import BeatSaberObjects.Objects.Enums.BeatmapCharacteristic;
import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Sidebar tab that lets the user create a new difficulty for a Beat Saber beatmap
 * characteristic (OneSaber, NoArrows, 90Degree, …) from the active diff, or apply
 * the same transform in place.
 *
 * Implemented characteristics (OneSaber, NoArrows, 90Degree, 360Degree, Lightshow, Lawless) show an
 * Apply button that opens a confirmation popup with "New difficulty" / "Apply to active diff" / Cancel.
 * Not-yet-implemented characteristics show a disabled "Not implemented yet" button.
 */
public class CharacteristicsView extends javafx.scene.control.ScrollPane {

    private final AppController controller;
    private final VBox content = new VBox(16);
    private final Label activeDiffLabel = new Label();
    private final Label result = new Label();

    public CharacteristicsView(AppController controller) {
        this.controller = controller;
        setFitToWidth(true);
        setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        content.setPadding(new Insets(16));
        setContent(content);

        Label title = new Label("Beatmap characteristics");
        title.getStyleClass().add(Styles.TITLE_3);
        activeDiffLabel.getStyleClass().add(Styles.TEXT_MUTED);

        // One-saber: keep the inverted label convention from UtilitiesView —
        // the button text shows what to KEEP, the function removes the other color.
        ToggleGroup saberGroup = new ToggleGroup();
        ToggleButton keepBlue = new ToggleButton("Blue");
        keepBlue.setToggleGroup(saberGroup);
        keepBlue.setTooltip(new javafx.scene.control.Tooltip("Keep blue notes, remove red (one-saber)"));
        ToggleButton keepRed = new ToggleButton("Red");
        keepRed.setToggleGroup(saberGroup);
        keepRed.setTooltip(new javafx.scene.control.Tooltip("Keep red notes, remove blue (one-saber)"));
        keepRed.setSelected(true);
        HBox saberToggle = new HBox(4, keepBlue, keepRed);
        saberToggle.setAlignment(Pos.CENTER_LEFT);

        VBox oneSaberRow = characteristicRow(
                BeatmapCharacteristic.ONE_SABER,
                "Keep notes of one color only. Select which color to keep.",
                saberToggle,
                () -> {
                    int removeType = keepBlue.isSelected() ? 0 : 1;
                    applyWithConfirm(BeatmapCharacteristic.ONE_SABER, removeType);
                });

        VBox noArrowsRow = characteristicRow(
                BeatmapCharacteristic.NO_ARROWS,
                "Turns every note into a dot note.",
                null,
                () -> applyWithConfirm(BeatmapCharacteristic.NO_ARROWS, -1));

        VBox deg90Row    = characteristicRow(BeatmapCharacteristic.DEGREE_90,  "Adds 90° lane-rotation events synced to sections, beats and note position.", null,
                () -> applyWithConfirm(BeatmapCharacteristic.DEGREE_90, -1));
        VBox deg360Row   = characteristicRow(BeatmapCharacteristic.DEGREE_360, "Adds 360° lane-rotation events synced to sections, beats and note position.", null,
                () -> applyWithConfirm(BeatmapCharacteristic.DEGREE_360, -1));
        VBox lightRow    = characteristicRow(BeatmapCharacteristic.LIGHTSHOW,  "Lightshow-only map. Strips all notes and obstacles; keeps all light events.", null,
                () -> applyWithConfirm(BeatmapCharacteristic.LIGHTSHOW, -1));
        VBox lawlessRow  = characteristicRow(BeatmapCharacteristic.LAWLESS,    "Adds intensity-scaled chaos: stacks, ghost notes and walls — parity rules intentionally ignored.", null,
                () -> applyWithConfirm(BeatmapCharacteristic.LAWLESS, -1));
        VBox legacyRow   = characteristicRow(BeatmapCharacteristic.LEGACY,     "Legacy map format (transform not yet implemented).", null, null);

        result.setWrapText(true);
        result.setMinHeight(Region.USE_PREF_SIZE);

        content.getChildren().addAll(
                title, activeDiffLabel,
                oneSaberRow, noArrowsRow,
                deg90Row, deg360Row, lightRow, lawlessRow, legacyRow,
                result);
        refresh();

        controller.addListener(new AppController.Listener() {
            @Override public void onStateChanged(AppState state) { Platform.runLater(CharacteristicsView.this::refresh); }
            @Override public void onActiveDiffChanged(DiffSession d) { Platform.runLater(CharacteristicsView.this::refresh); }
        });
    }

    /**
     * Shows a confirmation popup and either creates a new characteristic diff or applies
     * the transform in place, depending on the user's choice. If a diff with the target
     * filename already exists in the session, an overwrite confirmation is shown first.
     *
     * @param characteristic the target characteristic
     * @param removeType     note type to remove for ONE_SABER (0=red/1=blue); -1 for others
     */
    private void applyWithConfirm(BeatmapCharacteristic characteristic, int removeType) {
        DiffSession active = controller.getActiveDiff();
        if (active == null) { result.setText("No active diff selected."); return; }

        ButtonType newDiff    = new ButtonType("New difficulty",       ButtonBar.ButtonData.LEFT);
        ButtonType inPlace    = new ButtonType("Apply to active diff", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Cancel",               ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Apply " + characteristic.infoName);
        alert.setHeaderText("Apply " + characteristic.infoName + " to \"" + active.difficultyFileName() + "\"");
        alert.setContentText(
                "New difficulty — clones the active diff under the " + characteristic.infoName +
                " characteristic. Original is kept intact.\n\n" +
                "Apply to active diff — transforms the current diff in place (destructive).");
        alert.getButtonTypes().setAll(newDiff, inPlace, cancelType);

        alert.showAndWait().ifPresent(choice -> {
            if (choice == newDiff) {
                // Try without overwrite first; if collision, ask user
                DiffSession created = controller.createCharacteristicDiff(active, characteristic, removeType, false);
                if (created == null) {
                    created = confirmAndOverwrite(active, characteristic, removeType);
                }
                if (created != null) {
                    result.setText("✓ Created " + created.difficultyFileName() + " (" + characteristic.infoName + ")");
                }
            } else if (choice == inPlace) {
                switch (characteristic) {
                    case ONE_SABER -> {
                        controller.deleteNoteType(java.util.List.of(active), removeType);
                        result.setText("✓ Applied One-saber to " + active.difficultyFileName() + " (in place)");
                    }
                    case NO_ARROWS -> {
                        controller.makeNoArrows(java.util.List.of(active));
                        result.setText("✓ Applied No-arrows to " + active.difficultyFileName() + " (in place)");
                    }
                    case LIGHTSHOW -> {
                        active.map().makeLightshow();
                        result.setText("✓ Applied Lightshow to " + active.difficultyFileName() + " (in place)");
                    }
                    case DEGREE_360, DEGREE_90, LAWLESS -> {
                        controller.applyCharacteristicInPlace(java.util.List.of(active), characteristic, -1);
                        result.setText("✓ Applied " + characteristic.infoName + " to " + active.difficultyFileName() + " (in place)");
                    }
                    default -> result.setText("In-place transform not available for " + characteristic.infoName);
                }
            }
            // CANCEL: do nothing
        });
    }

    /**
     * Shows an overwrite confirmation dialog for a diff that already exists in the session.
     * Returns the created DiffSession if the user confirms, null if they cancel.
     */
    private DiffSession confirmAndOverwrite(DiffSession source, BeatmapCharacteristic characteristic, int removeType) {
        String base = BeatmapCharacteristic.baseDifficulty(source.difficultyFileName());
        String existingName = base + characteristic.filenameSuffix + ".dat";

        ButtonType overwrite  = new ButtonType("Overwrite",  ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Cancel",      ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert confirm = new Alert(Alert.AlertType.WARNING);
        confirm.setTitle("Diff already exists");
        confirm.setHeaderText("\"" + existingName + "\" already exists in this session.");
        confirm.setContentText("Overwrite it with a new " + characteristic.infoName + " diff from \"" + source.difficultyFileName() + "\"?");
        confirm.getButtonTypes().setAll(overwrite, cancelType);

        boolean doOverwrite = confirm.showAndWait().filter(b -> b == overwrite).isPresent();
        if (!doOverwrite) {
            result.setText("Cancelled — existing diff kept.");
            return null;
        }
        return controller.createCharacteristicDiff(source, characteristic, removeType, true);
    }

    /**
     * Builds one bordered characteristic row. If {@code action} is null (not-yet-implemented),
     * shows a disabled "Not implemented yet" button. Otherwise shows an "Apply" button.
     *
     * @param characteristic the characteristic this row represents
     * @param description    short description shown as muted text
     * @param params         optional extra controls (e.g. color toggle for OneSaber), may be null
     * @param action         action on Apply; null means not-yet-implemented
     */
    private VBox characteristicRow(BeatmapCharacteristic characteristic, String description,
                                   HBox params, Runnable action) {
        Label titleLabel = new Label(characteristic.infoName);
        titleLabel.getStyleClass().add(Styles.TITLE_4);

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add(Styles.TEXT_MUTED);
        descLabel.setWrapText(true);
        descLabel.setMinHeight(Region.USE_PREF_SIZE);

        Button applyBtn = new Button(characteristic.implemented ? "Apply…" : "Not implemented yet");
        applyBtn.setDisable(!characteristic.implemented);
        applyBtn.setTooltip(new javafx.scene.control.Tooltip(characteristic.implemented
                ? "Create a new " + characteristic.infoName + " difficulty from the active diff, or apply the transform in place."
                : "This characteristic isn't implemented yet."));
        if (action != null) applyBtn.setOnAction(e -> action.run());

        HBox controls = new HBox(8);
        controls.setAlignment(Pos.CENTER_LEFT);
        if (params != null) {
            params.setAlignment(Pos.CENTER_LEFT);
            controls.getChildren().add(params);
        }
        controls.getChildren().add(applyBtn);

        VBox box = new VBox(8, titleLabel, descLabel, controls);
        box.setPadding(new Insets(12));
        box.getStyleClass().add("bk-card");
        return box;
    }

    private void refresh() {
        boolean loaded = controller.state() != AppState.EMPTY;
        DiffSession active = controller.getActiveDiff();
        activeDiffLabel.setText(!loaded
                ? "Load a map first (1 · Load)."
                : active == null ? "No diff selected." : "Active diff: " + active.difficultyFileName());
        content.getChildren().stream()
                .filter(n -> n instanceof VBox)
                .forEach(n -> n.setDisable(!loaded));
    }
}
