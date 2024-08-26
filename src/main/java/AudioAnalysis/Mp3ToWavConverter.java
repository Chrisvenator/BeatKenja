package AudioAnalysis;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.SampleBuffer;
import lombok.Cleanup;

import javax.sound.sampled.*;
import java.io.*;


/**
 * A utility class that provides methods to convert MP3 files to WAV format.
 * This conversion is useful for applications that require audio in WAV format for processing or analysis.
 * The class utilizes the JavaZoom JLayer library to decode MP3 files into PCM data, which is then written as a WAV file.
 */
public class Mp3ToWavConverter {

    /**
     * Converts an MP3 file to a WAV file.
     * The method reads the MP3 file, decodes it to PCM audio data, and then writes the data to a WAV file.
     *
     * @param mp3FilePath The path to the input MP3 file.
     * @param wavFilePath The path where the output WAV file should be saved.
     * @throws IOException If an I/O error occurs during the conversion process.
     */
    public static void convert(String mp3FilePath, String wavFilePath) throws IOException {
        File file = new File(mp3FilePath);
        @Cleanup FileInputStream fis = new FileInputStream(file);
        @Cleanup BufferedInputStream bis = new BufferedInputStream(fis);
        try (AudioInputStream outAIS = getAudioInputStream(bis)) {
            File outFile = new File(wavFilePath);
            AudioSystem.write(outAIS, AudioFileFormat.Type.WAVE, outFile);
        }
    }

    /**
     * Creates an {@link AudioInputStream} from the given MP3 input stream.
     * The stream is decoded from MP3 format to PCM format using the JLayer library.
     *
     * @param is The input stream containing the MP3 data.
     * @return An {@link AudioInputStream} containing the decoded PCM data.
     */
    private static AudioInputStream getAudioInputStream(InputStream is) {
        Bitstream bitstream = new Bitstream(is);
        Decoder decoder = new Decoder();

        AudioFormat baseFormat = new AudioFormat(decoder.getOutputFrequency(),
                16,
                decoder.getOutputChannels(),
                true,
                false);

        return new AudioInputStream(new Mp3ToPcmAudioStream(bitstream, decoder), baseFormat, AudioSystem.NOT_SPECIFIED);
    }

    /**
     * A custom InputStream that decodes MP3 data to PCM audio data.
     * This class wraps around the JLayer library's {@link Bitstream} and {@link Decoder} classes
     * to convert MP3 frames into PCM samples that can be read sequentially.
     */
    static class Mp3ToPcmAudioStream extends InputStream {
        /** The Bitstream instance used to read MP3 frames. This object handles the low-level MP3 decoding operations.*/
        private final Bitstream bitstream;
        /** The Decoder instance used to decode MP3 frames into PCM samples. This object converts the compressed MP3 data into raw audio samples.*/
        private final Decoder decoder;
        /** The buffer containing the decoded PCM samples from the most recently decoded frame.*/
        private SampleBuffer buffer;
        /** The current index within the buffer, indicating the next sample to read.*/
        private int index = 0;

        /**
         * Constructs a new Mp3ToPcmAudioStream with the given Bitstream and Decoder.
         * The stream reads MP3 frames from the Bitstream, decodes them using the Decoder,
         * and provides the PCM data for reading.
         *
         * @param bitstream The Bitstream to read MP3 data from.
         * @param decoder   The Decoder to decode MP3 data into PCM samples.
         */
        public Mp3ToPcmAudioStream(Bitstream bitstream, Decoder decoder) {
            this.bitstream = bitstream;
            this.decoder = decoder;
        }

        /**
         * Reads the next byte of PCM data from the stream.
         * If the current buffer is exhausted, the method decodes the next MP3 frame to refill the buffer.
         *
         * @return The next byte of PCM data, or -1 if the end of the stream has been reached.
         */
        @Override
        public int read()
        {
            if (buffer == null || index >= buffer.getBufferLength()) {
                if (!decodeFrame()) {
                    return -1;  // end of stream
                }
                index = 0;  // reset index to start of buffer
            }
            return buffer.getBuffer()[index++] & 0xff;  // return next byte
        }

        /**
         * Decodes the next MP3 frame and stores the resulting PCM samples in the buffer.
         * The method returns false if no more frames are available, indicating the end of the stream.
         *
         * @return true if a frame was successfully decoded, false if there are no more frames.
         */
        private boolean decodeFrame() {
            try {
                buffer = (SampleBuffer) decoder.decodeFrame(bitstream.readFrame(), bitstream);
                if (buffer == null) {
                    return false;
                }
                bitstream.closeFrame();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
