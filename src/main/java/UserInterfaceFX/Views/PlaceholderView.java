package UserInterfaceFX.Views;

import atlantafx.base.theme.Styles;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Simple stand-in for views that arrive in later stages. */
public class PlaceholderView extends VBox {

    public PlaceholderView(String title, String description) {
        super(8);
        setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add(Styles.TITLE_2);

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add(Styles.TEXT_MUTED);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(480);

        getChildren().addAll(titleLabel, descriptionLabel);
    }
}