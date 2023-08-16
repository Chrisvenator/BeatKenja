import sys
try:
    from pydub import AudioSegment
    print("pydub is installed")
except ImportError:
    sys.exit(-4)

from pydub import AudioSegment


def convert_to_wav(input_file, output_file):
    song = AudioSegment.from_file(input_file)
    song.export(output_file, format="wav")

#!!! OGG IS BROKEN !!!
def convert_to_ogg(input_file, output_file):
    song = AudioSegment.from_mp3(input_file)
    song.export(output_file, format="ogg")


if __name__ == "__main__":
    if len(sys.argv) < 4:
        print("Usage: python convert_song.py <input_file> <output_file> <format>")
        sys.exit(1)

    input_file = sys.argv[1]
    output_file = sys.argv[2]
    target_format = sys.argv[3].lower()

    if target_format == "wav":
        convert_to_wav(input_file, output_file)
        print(f"Converted {input_file} to WAV format: {output_file}")
        sys.exit(0)
    elif target_format == "ogg":
        convert_to_ogg(input_file, output_file)
        print(f"Converted {input_file} to OGG format: {output_file}")
        sys.exit(0)
    else:
        print("Supported formats: wav, ogg")
        sys.exit(1)
