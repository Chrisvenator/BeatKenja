package AudioAnalysis;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
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
    private static AudioInputStream getAudioInputStream(InputStream is) throws IOException {
        Bitstream bitstream = new Bitstream(is);
        Decoder decoder = new Decoder();
        
        // Read the first frame to get audio format information
        Header header;
        try {
            header = bitstream.readFrame();
            if (header == null) {
                throw new IOException("Invalid MP3 file - no frames found");
            }
        } catch (Exception e) {
            throw new IOException("Error reading MP3 header: " + e.getMessage(), e);
        }
        
        // Get audio format information from the header
        int sampleRate = header.frequency();
        int channels = (header.mode() == Header.SINGLE_CHANNEL) ? 1 : 2;
        
        // Create audio format for 16-bit signed PCM, little-endian
        AudioFormat audioFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                sampleRate,
                16,              // 16 bits per sample
                channels,        // mono or stereo
                channels * 2,    // frame size in bytes (2 bytes per sample per channel)
                sampleRate,      // frame rate (same as sample rate for PCM)
                false            // little-endian
        );
        
        // Reset the bitstream to the beginning
        bitstream.closeFrame();
        
        return new AudioInputStream(new Mp3ToPcmAudioStream(bitstream, decoder, channels),
                audioFormat, AudioSystem.NOT_SPECIFIED);
    }
    
    /**
     * A custom InputStream that decodes MP3 data to PCM audio data.
     * This class wraps around the JLayer library's {@link Bitstream} and {@link Decoder} classes
     * to convert MP3 frames into PCM samples that can be read sequentially.
     */
    static class Mp3ToPcmAudioStream extends InputStream {
        /** The Bitstream instance used to read MP3 frames. */
        private final Bitstream bitstream;
        /** The Decoder instance used to decode MP3 frames into PCM samples. */
        private final Decoder decoder;
        /** Number of audio channels (1 for mono, 2 for stereo). */
        private final int channels;
        /** The byte array containing the current frame's PCM data. */
        private byte[] frameBytes;
        /** The current byte index within the frame bytes array. */
        private int byteIndex = 0;
        /** Flag indicating if the stream has ended. */
        private boolean streamEnded = false;
        
        /**
         * Constructs a new Mp3ToPcmAudioStream.
         *
         * @param bitstream The Bitstream to read MP3 data from.
         * @param decoder   The Decoder to decode MP3 data into PCM samples.
         * @param channels  The number of audio channels.
         */
        public Mp3ToPcmAudioStream(Bitstream bitstream, Decoder decoder, int channels) {
            this.bitstream = bitstream;
            this.decoder = decoder;
            this.channels = channels;
        }
        
        /**
         * Reads the next byte of PCM data from the stream.
         *
         * @return The next byte of PCM data, or -1 if the end of the stream has been reached.
         */
        @Override
        public int read() {
            if (streamEnded) {
                return -1;
            }
            
            if (frameBytes == null || byteIndex >= frameBytes.length) {
                if (!decodeNextFrame()) {
                    streamEnded = true;
                    return -1;
                }
                byteIndex = 0;
            }
            
            return frameBytes[byteIndex++] & 0xFF;
        }
        
        /**
         * Reads up to len bytes of data from the input stream into an array of bytes.
         *
         * @param b   the buffer into which the data is read.
         * @param off the start offset in array b at which the data is written.
         * @param len the maximum number of bytes to read.
         * @return the total number of bytes read into the buffer, or -1 if end of stream.
         */
        @Override
        public int read(byte[] b, int off, int len) {
            if (streamEnded) {
                return -1;
            }
            
            if (b == null) {
                throw new NullPointerException();
            }
            if (off < 0 || len < 0 || len > b.length - off) {
                throw new IndexOutOfBoundsException();
            }
            if (len == 0) {
                return 0;
            }
            
            int totalBytesRead = 0;
            
            while (totalBytesRead < len && !streamEnded) {
                if (frameBytes == null || byteIndex >= frameBytes.length) {
                    if (!decodeNextFrame()) {
                        streamEnded = true;
                        break;
                    }
                    byteIndex = 0;
                }
                
                int remainingInFrame = frameBytes.length - byteIndex;
                int bytesToCopy = Math.min(len - totalBytesRead, remainingInFrame);
                
                System.arraycopy(frameBytes, byteIndex, b, off + totalBytesRead, bytesToCopy);
                byteIndex += bytesToCopy;
                totalBytesRead += bytesToCopy;
            }
            
            return totalBytesRead == 0 ? -1 : totalBytesRead;
        }
        
        /**
         * Decodes the next MP3 frame and converts it to PCM byte data.
         *
         * @return true if a frame was successfully decoded, false if end of stream.
         */
        private boolean decodeNextFrame() {
            try {
                Header header = bitstream.readFrame();
                if (header == null) {
                    return false;
                }
                
                SampleBuffer sampleBuffer = (SampleBuffer) decoder.decodeFrame(header, bitstream);
                if (sampleBuffer == null) {
                    return false;
                }
                
                // Convert the sample buffer to PCM bytes
                frameBytes = convertSamplesToBytes(sampleBuffer);
                
                bitstream.closeFrame();
                return true;
                
            } catch (Exception e) {
                return false;
            }
        }
        
        /**
         * Converts SampleBuffer data to PCM byte array.
         *
         * @param sampleBuffer The sample buffer from JLayer decoder.
         * @return PCM data as byte array.
         */
        private byte[] convertSamplesToBytes(SampleBuffer sampleBuffer) {
            short[] samples = sampleBuffer.getBuffer();
            int sampleCount = sampleBuffer.getBufferLength();
            
            // For mono files, JLayer duplicates samples for both channels
            // We need to handle this correctly based on the actual channel count
            int actualSamples = (channels == 1) ? sampleCount / 2 : sampleCount;
            byte[] pcmBytes = new byte[actualSamples * 2]; // 2 bytes per 16-bit sample
            
            int byteIndex = 0;
            for (int i = 0; i < actualSamples; i++) {
                short sample = samples[i];
                
                // Convert to little-endian 16-bit PCM
                pcmBytes[byteIndex++] = (byte) (sample & 0xFF);        // Low byte
                pcmBytes[byteIndex++] = (byte) ((sample >> 8) & 0xFF); // High byte
            }
            
            return pcmBytes;
        }
        
        @Override
        public void close() throws IOException {
            try {
                bitstream.close();
            } catch (Exception e) {
                throw new IOException("Error closing bitstream", e);
            }
        }
    }
}