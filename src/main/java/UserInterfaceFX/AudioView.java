package UserInterfaceFX;

/**
 * A view that hosts its own audio transport bar over the session's shared player.
 * <p>
 * Because one {@link AppLogic.AudioPreviewPlayer} is now shared across views, only the
 * visible view's transport should tick and own the play/pause visuals. The shell calls
 * {@link #onShown()} when the view becomes visible (re-sync + start ticking) and
 * {@link #onHidden()} when it is swapped out (stop ticking; playback keeps running so the
 * global spine still tracks it).
 */
public interface AudioView {

    /** Called when the view becomes visible: re-sync the transport to the shared player and resume ticking. */
    void onShown();

    /** Called when the view is swapped out: stop the transport's own timer (playback itself continues). */
    void onHidden();
}
