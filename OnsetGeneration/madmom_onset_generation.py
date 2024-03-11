import sys
import argparse
try:

    import madmom
    import numpy as np
    from scipy.ndimage.filters import maximum_filter
except ImportError:
    sys.exit(-4)

import madmom
import numpy as np
from scipy.ndimage.filters import maximum_filter

def main(audio_path, output_path, certainty, proximity):

    spec = madmom.audio.spectrogram.Spectrogram(audio_path, num_channels=1)

    # calculate the difference
    diff = np.diff(spec, axis=0)
    # keep only the positive differences
    pos_diff = np.maximum(0, diff)
    # sum everything to get the spectral flux
    sf = np.sum(pos_diff, axis=1)

    sf = madmom.features.onsets.spectral_flux(spec)

    filt_spec = madmom.audio.spectrogram.FilteredSpectrogram(spec, filterbank=madmom.audio.filters.LogFilterbank,
                                                             num_bands=24)

    log_spec = madmom.audio.spectrogram.LogarithmicSpectrogram(filt_spec, add=1)

    # maximum filter size spreads over 3 frequency bins
    size = (1, 3)
    max_spec = maximum_filter(log_spec, size=size)

    # init the diff array
    diff = np.zeros_like(log_spec)
    # calculate the difference between the log. spec and the max. filtered version thereof
    diff[1:] = (log_spec[1:] - max_spec[: -1])
    # then continue as with the spectral flux, i.e. keep only the positive differences
    pos_diff = np.maximum(0, diff)

    superflux = np.sum(pos_diff, axis=1)

    # Generate x coordinates as a range from 0 to the length of the superflux array
    x_coords = np.arange(len(superflux))

    # Pair each x coordinate with its corresponding superflux value
    coordinates = list(zip(x_coords, superflux))

    if output_path:
        with open(output_path, 'w') as file:
            prev = 0
            for x, y in coordinates:
                if y > certainty:
                    if (x - prev) / 100 >= proximity:
                        # print(f"X: {x / 100}, Y: {prev / 100}, Z: {(x-prev)/100}")
                        file.write(str(x / 100) + '\n')
                    prev = x
        print("Saved to file:", output_path)

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Audio Analysis Script')
    parser.add_argument('audio_path', type=str, help='Path to the audio file')
    parser.add_argument('-o', '--output', type=str, help='Output file path')
    parser.add_argument('-p', '--madmom_proximity', type=float, help='How close to each other the notes may spawn (in seconds)')
    parser.add_argument('-c', '--madmom_certainty', type=float, help='How certain the note placements should be')
    args = parser.parse_args()

    main(args.audio_path, args.output, args.madmom_certainty, args.madmom_proximity)