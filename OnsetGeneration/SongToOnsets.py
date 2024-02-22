import sys
try:
    import librosa
    print("Librosa is installed")
except ImportError:
    sys.exit(-4)

import argparse
import librosa

def main(audio_path, output_path):
    # Lade den Song und extrahiere die Onsets
    y, sr = librosa.load(audio_path)
    onset_frames = librosa.onset.onset_detect(y=y, sr=sr)

    # Konvertiere die Onset-Frames in Sekunden
    onset_times = librosa.frames_to_time(onset_frames, sr=sr)

    # Gib die gefundenen Onset-Zeiten aus
    # for onset_time in onset_times:
    #     print(onset_time)

    # Schreibe die gefundenen Onset-Zeiten in die Datei
    if output_path:
        with open(output_path, 'w') as file:
            for onset_time in onset_times:
                file.write(str(onset_time) + '\n')
        print("Saved to file:", output_path)

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Audio Analysis Script')
    parser.add_argument('audio_path', type=str, help='Path to the audio file')
    parser.add_argument('-o', '--output', type=str, help='Output file path')
    args = parser.parse_args()

    main(args.audio_path, args.output)