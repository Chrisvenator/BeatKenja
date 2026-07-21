package UserInterfaceFX.Views;

import AppLogic.AppController;
import AppLogic.AppState;
import AppLogic.DiffSession;
import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

/**
 * Map utilities (formerly the Swing "Map Utilities" sub-buttons):
 * no-arrow conversion, flashing-light removal, note-type deletion, placement snapping.
 * Each utility can run on the active diff or on all loaded diffs.
 */
public class UtilitiesView extends VBox {

    private final AppController controller;
    private final Label activeDiffLabel = new Label();
    private final Label result = new Label();

    public UtilitiesView(AppController controller) {
        super(16);
        this.controller = controller;
        setPadding(new Insets(16));

        Label title = new Label("Map utilities");
        title.getStyleClass().add(Styles.TITLE_3);
        activeDiffLabel.getStyleClass().add(Styles.TEXT_MUTED);

        VBox noArrows = utilityRow(
                "No-arrow diff",
                "Turns every note into a dot note.",
                null,
                targets -> {
                    controller.makeNoArrows(targets);
                    result.setText("✓ Converted " + targets.size() + " diff(s) to no arrows");
                });

        VBox lights = utilityRow(
                "Convert flashing lights",
                "Replaces all flashing light events with regular \"on\" events.",
                null,
                targets -> {
                    controller.convertFlashingLights(targets);
                    result.setText("✓ Converted flashing lights in " + targets.size() + " diff(s)");
                });

        // Note type as red/blue toggle instead of the old free-text 0/1 field
        ToggleGroup noteType = new ToggleGroup();
        ToggleButton red = new ToggleButton("Red (0)");
        red.setToggleGroup(noteType);
        ToggleButton blue = new ToggleButton("Blue (1)");
        blue.setToggleGroup(noteType);
        blue.setSelected(true);
        VBox deleteType = utilityRow(
                "Delete note color",
                "Removes all notes of one color, e.g. to make a one-handed diff.",
                new HBox(4, red, blue),
                targets -> {
                    int type = red.isSelected() ? 0 : 1;
                    controller.deleteNoteType(targets, type);
                    result.setText("✓ Deleted all " + (type == 0 ? "red" : "blue") + " notes in " + targets.size() + " diff(s)");
                });

        TextField precision = new TextField("16");
        precision.setPrefWidth(70);
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

        result.setWrapText(true);
        result.setMinHeight(Region.USE_PREF_SIZE);

        getChildren().addAll(title, activeDiffLabel, noArrows, lights, deleteType, placements, result);
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

    /** One bordered utility row: title, description, optional parameter controls, run buttons. */
    private VBox utilityRow(String title, String description, HBox params, Consumer<List<DiffSession>> action) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add(Styles.TITLE_4);

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add(Styles.TEXT_MUTED);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMinHeight(Region.USE_PREF_SIZE);

        Button runActive = new Button("Active diff");
        runActive.setOnAction(e -> {
            if (controller.getActiveDiff() != null) action.accept(List.of(controller.getActiveDiff()));
        });

        Button runAll = new Button("All diffs");
        runAll.getStyleClass().add(Styles.FLAT);
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
        box.setStyle("-fx-border-color: -color-border-default; -fx-border-width: 1; -fx-border-radius: 8;");
        return box;
    }

    private void refresh() {
        boolean loaded = controller.state() != AppState.EMPTY;
        DiffSession active = controller.getActiveDiff();
        activeDiffLabel.setText(!loaded
                ? "Load a map first (1 · Load) — the utilities work on the loaded diffs."
                : active == null ? "No diff selected." : "Active diff: " + active.difficultyFileName());
        getChildren().stream().filter(n -> n instanceof VBox).forEach(n -> n.setDisable(!loaded));
    }
}
