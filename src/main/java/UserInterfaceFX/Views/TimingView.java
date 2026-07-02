package UserInterfaceFX.Views;

import AppLogic.AppController;
import AppLogic.AppState;
import AppLogic.DiffSession;
import atlantafx.base.theme.Styles;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Step 2: convert loaded diffs to timing notes.
 * Two cards: 1-color (the format the generators expect) and 2-color (legacy, shaky).
 * Each card converts the active diff; "Apply to all diffs" converts every loaded diff.
 */
public class TimingView extends VBox {

    private final AppController controller;
    private final Label activeDiffLabel = new Label();
    private final Label result = new Label();

    public TimingView(AppController controller) {
        super(16);
        this.controller = controller;
        setPadding(new Insets(16));

        activeDiffLabel.getStyleClass().add(Styles.TEXT_MUTED);

        VBox oneColorCard = card(
                "→ 1-color timing notes",
                "All notes become blue dot notes in the bottom-left corner. This is the required input format for the generators.",
                false, true);

        VBox twoColorCard = card(
                "→ 2-color timing notes",
                "Keeps red/blue split as dot notes. Likely broken (old UI warned as well) — use at your own risk.",
                true, false);

        HBox cards = new HBox(16, oneColorCard, twoColorCard);
        HBox.setHgrow(oneColorCard, Priority.ALWAYS);
        HBox.setHgrow(twoColorCard, Priority.ALWAYS);

        result.getStyleClass().add(Styles.SUCCESS);

        getChildren().addAll(activeDiffLabel, cards, result);
        refreshActiveDiff();

        controller.addListener(new AppController.Listener() {
            @Override
            public void onActiveDiffChanged(DiffSession activeDiff) {
                Platform.runLater(TimingView.this::refreshActiveDiff);
            }

            @Override
            public void onStateChanged(AppState state) {
                Platform.runLater(TimingView.this::refreshActiveDiff);
            }
        });
    }

    private VBox card(String title, String description, boolean warning, boolean primary) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add(Styles.TITLE_4);

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add(warning ? Styles.WARNING : Styles.TEXT_MUTED);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMinHeight(Region.USE_PREF_SIZE);

        boolean oneColor = !warning;

        Button convertActive = new Button("Convert active diff");
        if (primary) convertActive.getStyleClass().add(Styles.ACCENT);
        convertActive.setOnAction(e -> convert(oneColor, List.of(controller.getActiveDiff())));

        Button convertAll = new Button("Apply to all diffs");
        convertAll.getStyleClass().add(Styles.FLAT);
        convertAll.setOnAction(e -> convert(oneColor, controller.session().diffs()));

        VBox box = new VBox(10, titleLabel, descriptionLabel, new HBox(8, convertActive, convertAll));
        box.setPadding(new Insets(16));
        box.setStyle("-fx-border-color: -color-border-default; -fx-border-width: 1; -fx-border-radius: 8;");
        return box;
    }

    private void convert(boolean oneColor, List<DiffSession> targets) {
        if (targets.isEmpty() || targets.get(0) == null) return;
        controller.convertToTimingNotes(oneColor, List.copyOf(targets));
        result.setText("✓ Converted " + targets.size() + " diff(s) to " + (oneColor ? "1-color" : "2-color") + " timing notes — continue with 3 · Generate");
    }

    private void refreshActiveDiff() {
        DiffSession active = controller.getActiveDiff();
        activeDiffLabel.setText(active == null
                ? "No diff selected."
                : "Active diff: " + active.difficultyFileName() + " (switch via the tabs above)");
    }
}