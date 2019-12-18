# VoiceKit NodeJS examples

You will need nodejs and npm to run this examples.
To work correctly all scripts should be run from this directory (`voicekit-examples/nodejs`).

## Usage

### Install the requirements

```
$ npm install
```

### Basic recognition examples

Run basic (non-streaming) speech recognition example:

```
$ node recognize.js -e MPEG_AUDIO -r 16000 -c 1 ../audio/sample_1.mp3
```

To disable automatic punctuation and get up to 3 recognition alternatives:

```
$ node recognize.js -e MPEG_AUDIO -r 16000 -c 1 --no-automatic-punctuation --max-alternatives 3 ../audio/sample_1.mp3
```

Run streaming speech recognition with interim results and disabled voice activity detection (VAD):

```
$ node recognize_stream.js -e MPEG_AUDIO -r 16000 -c 1 --interim-results --no-perform-vad ../audio/sample_1.mp3
```

Specify longer silence timeout for voice activity detection (you will probably need longer audio to actually see the difference):

```
$ node recognize_stream.js -e MPEG_AUDIO -r 16000 -c 1 --interim-results --silence-duration-threshold 1.2 ../audio/sample_1.mp3
```

Return just the first recognized utterance and halt (you will probably need longer audio to actually see the difference):

```
$ node recognize_stream.js -e MPEG_AUDIO -r 16000 -c 1 --interim-results --single-utterance ../audio/sample_1.mp3
```

### Basic synthesis examples

To run basic speech synthesis and save result to wav:

```
$ node synthesize_stream.js -r 48000 -e LINEAR16 "И мысли тоже тяжелые и медлительные, падают неторопливо и редко одна за другой, точно песчинки в разленившихся песочных часах." output_1.wav
```

You can also specify alternative stress with `0` sign after a vowel:

```
$ node synthesize_stream.js -r 48000 -e LINEAR16 "За0мок - замо0к." output_2.wav
```

Feel free to use arabic numerals and named entities:

```
$ node synthesize_stream.js -r 48000 -e LINEAR16 "Газета Times, 03 января 2009 года - Канцлер на грани ради второго спасения банков." output_3.wav
```

For now, `LINEAR16` does not support samples rates other than 48kHz. Use `RAW_OPUS` (`node-opus` required) to specify different sample rates:

```
$ node synthesize_stream.js -r 16000 -e RAW_OPUS "Привет, мир." output_4.wav
```
