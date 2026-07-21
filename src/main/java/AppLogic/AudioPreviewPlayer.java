package AppLogic;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;

/**
 * Small wav preview player for the Song Map card: play/pause and seeking so the user can
 * listen to a spot while looking at the section visualization. Wraps a {@link Clip}, which
 * loads the whole file into memory — fine for previewing one song, not for batch use.
 */
public final class AudioPreviewPlayer implements AutoCloseable {

    /** The clip playback currently runs on: {@link #cleanClip} or {@link #clickClip}. */
    private Clip clip;
    private Clip cleanClip;
    private Clip clickClip;
    private boolean clickTrackEnabled;
    /** Linear volume 0..1, remembered across {@link #load} calls. */
    private float volume = 1.0f;

    /**
     * Loads a wav file for playback, stopping and replacing any previously loaded one
     * (including a click track). Non-PCM or &gt;16-bit files are converted to 16-bit
     * signed PCM so the clip accepts them.
     *
     * @param wavFile the wav to play
     * @throws Exception if the file cannot be read or no audio output line is available
     */
    public synchronized void load(File wavFile) throws Exception {
        close();
        cleanClip = openClip(wavFile);
        clip = cleanClip;
        applyVolume();
    }

    /**
     * Loads the click-track rendition of the current song (same length as the clean wav).
     * If the click track is currently enabled, playback switches to it seamlessly.
     *
     * @param wavFile the wav with clicks mixed in (see {@code ClickTrackRenderer})
     * @throws Exception if the file cannot be read or no audio output line is available
     */
    public synchronized void loadClickTrack(File wavFile) throws Exception {
        if (cleanClip == null) return;
        if (clickClip != null) clickClip.close();
        clickClip = openClip(wavFile);
        if (clickTrackEnabled) switchTo(clickClip);
    }

    public synchronized boolean hasClickTrack() {
        return clickClip != null;
    }

    /** Switches between the clean and the click-track clip, keeping position and play state. */
    public synchronized void setClickTrackEnabled(boolean enabled) {
        clickTrackEnabled = enabled;
        switchTo(enabled ? clickClip : cleanClip);
    }

    private void switchTo(Clip target) {
        if (target == null || target == clip) return;
        boolean wasPlaying = clip != null && clip.isRunning();
        long position = clip != null ? clip.getMicrosecondPosition() : 0;
        if (clip != null) clip.stop();
        target.setMicrosecondPosition(position);
        clip = target;
        applyVolume();
        if (wasPlaying) target.start();
    }

    private static Clip openClip(File wavFile) throws Exception {
        AudioInputStream in = AudioSystem.getAudioInputStream(wavFile);
        AudioFormat format = in.getFormat();
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED || format.getSampleSizeInBits() > 16) {
            AudioFormat target = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(), 16, format.getChannels(),
                    format.getChannels() * 2, format.getSampleRate(), false);
            in = AudioSystem.getAudioInputStream(target, in);
        }
        Clip loaded = AudioSystem.getClip();
        loaded.open(in);
        return loaded;
    }

    /** Sets the playback volume (0 = mute, 1 = full); kept across subsequently loaded files. */
    public synchronized void setVolume(double volume) {
        this.volume = (float) Math.max(0, Math.min(1, volume));
        applyVolume();
    }

    /** Translates the linear 0..1 volume to the clip's dB master-gain control. */
    private void applyVolume() {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float dB = volume <= 0.001f ? gain.getMinimum() : (float) (20.0 * Math.log10(volume));
        gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
    }

    public synchronized boolean isLoaded() {
        return clip != null;
    }

    public synchronized boolean isPlaying() {
        return clip != null && clip.isRunning();
    }

    /** Starts (or resumes) playback; restarts from the beginning if the song had finished. */
    public synchronized void play() {
        if (clip == null) return;
        if (clip.getMicrosecondPosition() >= clip.getMicrosecondLength()) clip.setMicrosecondPosition(0);
        clip.start();
    }

    public synchronized void pause() {
        if (clip != null) clip.stop();
    }

    /** Jumps to the given position, keeping the current play/pause state. */
    public synchronized void seekSeconds(double seconds) {
        if (clip == null) return;
        boolean wasPlaying = clip.isRunning();
        clip.stop();
        long micros = (long) (seconds * 1_000_000L);
        clip.setMicrosecondPosition(Math.max(0, Math.min(micros, clip.getMicrosecondLength())));
        if (wasPlaying) clip.start();
    }

    public synchronized double positionSeconds() {
        return clip == null ? 0 : clip.getMicrosecondPosition() / 1_000_000.0;
    }

    public synchronized double durationSeconds() {
        return clip == null ? 0 : clip.getMicrosecondLength() / 1_000_000.0;
    }

    /** Stops playback and releases the audio lines (clean and click track). */
    @Override
    public synchronized void close() {
        if (cleanClip != null) {
            cleanClip.stop();
            cleanClip.close();
        }
        if (clickClip != null) {
            clickClip.stop();
            clickClip.close();
        }
        clip = cleanClip = clickClip = null;
        clickTrackEnabled = false;
    }
}
