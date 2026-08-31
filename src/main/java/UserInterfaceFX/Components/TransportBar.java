package UserInterfaceFX.Components;

import AppLogic.AudioPreviewPlayer;
import atlantafx.base.theme.Styles;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.function.DoubleConsumer;

/**
 * Shared audio transport bar: play/pause, a scrub slider, a time readout and volume.
 *
 * Used by the Timing "Song Map" card and the 3D Viewer so both share one playback UI and
 * behaviour — scrub-while-dragging, seek-on-release, and a per-frame playhead callback the
 * host uses to redraw its own visuals (section canvas, note field, timeline). The bar drives
 * an injected {@link AudioPreviewPlayer} but does NOT own its lifecycle: the view that created
 * the player closes it (see {@link #stopTimer()}). View-specific controls (a click-track
 * checkbox, a note/onset toggle) are added on the right via {@link #setTrailing}.
 */
public final class TransportBar extends HBox {

    private final AudioPreviewPlayer player;
    private final FontIcon playIcon = new FontIcon(Feather.PLAY);
    private final Button playButton = new Button(null, playIcon);
    private final Slider positionSlider = new Slider(0, 1, 0);
    private final Label timeLabel = new Label("0:00 / 0:00");
    private final HBox trailing = new HBox(8);

    /** Fired with the current position (seconds) every frame while playing and on every seek/scrub. */
    private DoubleConsumer onPlayhead = seconds -> { };

    private final AnimationTimer playheadTimer = new AnimationTimer() {
        @Override public void handle(long now) { tick(); }
    };

    public TransportBar(AudioPreviewPlayer player) {
        super(8);
        this.player = player;
        setAlignment(Pos.CENTER_LEFT);

        playButton.getStyleClass().add(Styles.BUTTON_ICON);
        playButton.setTooltip(new Tooltip("Play / pause"));
        playButton.setDisable(true);
        playButton.setOnAction(e -> toggle());

        positionSlider.setDisable(true);
        positionSlider.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(positionSlider, Priority.ALWAYS);
        // Drag = preview only; the real seek happens on release so we don't thrash the audio line.
        positionSlider.valueProperty().addListener((obs, old, v) -> {
            if (positionSlider.isValueChanging()) scrub(v.doubleValue());
        });
        positionSlider.setOnMouseReleased(e -> seek(positionSlider.getValue()));

        timeLabel.getStyleClass().add(Styles.TEXT_MUTED);

        FontIcon volumeIcon = new FontIcon(Feather.VOLUME_2);
        Slider volumeSlider = new Slider(0, 1, 1);
        volumeSlider.setPrefWidth(90);
        volumeSlider.setMinWidth(60);
        volumeSlider.valueProperty().addListener((obs, old, v) -> player.setVolume(v.doubleValue()));

        trailing.setAlignment(Pos.CENTER_LEFT);
        getChildren().addAll(playButton, positionSlider, timeLabel, volumeIcon, volumeSlider, trailing);
    }

    /** Registers the per-frame playhead callback; the host redraws its own visuals from the seconds value. */
    public void setOnPlayhead(DoubleConsumer callback) {
        this.onPlayhead = callback == null ? seconds -> { } : callback;
    }

    /** Adds view-specific controls (e.g. a click-track checkbox) to the right of the volume slider. */
    public void setTrailing(Node... nodes) {
        trailing.getChildren().setAll(nodes);
    }

    /**
     * Enables the transport for a freshly loaded song and resets the playhead to the start.
     * Call once the injected player has loaded a wav.
     */
    public void onLoaded() {
        playButton.setDisable(false);
        positionSlider.setDisable(false);
        positionSlider.setMax(player.durationSeconds());
        positionSlider.setValue(0);
        timeLabel.setText("0:00 / " + formatTime(player.durationSeconds()));
        setPlaying(false);
    }

    /** Stops playback visuals and disables the controls (e.g. before re-analyzing a new song). */
    public void reset() {
        playheadTimer.stop();
        setPlaying(false);
        playButton.setDisable(true);
        positionSlider.setDisable(true);
        positionSlider.setValue(0);
        timeLabel.setText("0:00 / 0:00");
    }

    /** Jumps to the given position and moves the slider there; also used by external seeks (canvas / timeline clicks). */
    public void seek(double seconds) {
        if (!player.isLoaded()) return;
        player.seekSeconds(seconds);
        if (positionSlider.getValue() != seconds) positionSlider.setValue(seconds);
        tick();
    }

    /** Stops the animation timer (call on view shutdown; the player is closed by its owner). */
    public void stopTimer() {
        playheadTimer.stop();
    }

    public static String formatTime(double seconds) {
        int s = (int) Math.max(0, seconds);
        return String.format("%d:%02d", s / 60, s % 60);
    }

    private void toggle() {
        if (!player.isLoaded()) return;
        if (player.isPlaying()) {
            player.pause();
            playheadTimer.stop();
            setPlaying(false);
            tick();
        } else {
            player.play();
            setPlaying(true);
            playheadTimer.start();
        }
    }

    private void scrub(double seconds) {
        timeLabel.setText(formatTime(seconds) + " / " + formatTime(player.durationSeconds()));
        onPlayhead.accept(seconds);
    }

    private void tick() {
        double position = player.positionSeconds();
        if (!positionSlider.isValueChanging()) positionSlider.setValue(position);
        timeLabel.setText(formatTime(position) + " / " + formatTime(player.durationSeconds()));
        onPlayhead.accept(position);
        if (!player.isPlaying()) {
            playheadTimer.stop();
            setPlaying(false);
        }
    }

    private void setPlaying(boolean playing) {
        playIcon.setIconCode(playing ? Feather.PAUSE : Feather.PLAY);
    }
}
