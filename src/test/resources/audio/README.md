# Audio import fixture

`tone.mp3.b64` is an original synthetic 440 Hz mono sine, 44.1 kHz, 0.25 seconds,
amplitude 0.125, encoded at 128 kbit/s and stored as Base64 text. It contains no
third-party recording. It is covered by the project's GPL-3.0-only license.

Generated once using FFmpeg's sine source and libmp3lame. Tests decode the fixed
fixture; they do not invoke or require an MP3 encoder or FFmpeg installation.
WAV fixtures are synthesized directly in the Java tests.

The vendored decoder retains MP3 encoder padding: this fixture produces 15047
frames at 48 kHz, while the equivalent WAV produces 12000. This is asserted
explicitly; gapless MP3 metadata trimming is not promised.
