package AudioAnalysis;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;

import javax.sound.sampled.*;
import java.io.*;

public class Mp3ToWavConverter {

    public static void convert(String mp3FilePath, String wavFilePath) throws IOException
    {
        File file = new File(mp3FilePath);
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        try (AudioInputStream outAIS = getAudioInputStream(bis)) {
            File outFile = new File(wavFilePath);
            AudioSystem.write(outAIS, AudioFileFormat.Type.WAVE, outFile);
        }
    }

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

    static class Mp3ToPcmAudioStream extends InputStream {
        private final Bitstream bitstream;
        private final Decoder decoder;
        private SampleBuffer buffer;
        private int index = 0;

        public Mp3ToPcmAudioStream(Bitstream bitstream, Decoder decoder) {
            this.bitstream = bitstream;
            this.decoder = decoder;
        }

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
