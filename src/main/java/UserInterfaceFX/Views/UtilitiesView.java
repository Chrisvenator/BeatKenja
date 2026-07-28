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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
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

        content.getChildren().addAll(title, activeDiffLabel, lights, placements, result);
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

        Button runActive = new Button("Apply to active diff");
        runActive.setOnAction(e -> {
            if (controller.getActiveDiff() != null) action.accept(List.of(controller.getActiveDiff()));
        });

        Button runAll = new Button("Apply to all diffs");
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
        content.getChildren().stream().filter(n -> n instanceof VBox).forEach(n -> n.setDisable(!loaded));
    }
}
