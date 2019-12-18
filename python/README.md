# VoiceKit Python examples

You will need Python >= 3.5 to run this examples.
To work correctly all scripts should be run from this directory (`voicekit-examples/python`).

## Usage

### Install the requirements


```
$ python3 -m pip install -r requirements.txt
```

You may install optional dependencies (`opuslib` and `PyAudio`) to gain additional functionality:

```
$ python3 -m pip install -r requirements/all.txt
```


### Basic recognition examples

Run basic (non-streaming) speech recognition example:

```
$ python3 recognize.py -r 16000 -c 1 -e MPEG_AUDIO ../audio/sample_1.mp3
```

To disable automatic punctuation and get up to 3 recognition alternatives:

```
$ python3 recognize.py -r 16000 -c 1 -e MPEG_AUDIO --disable_automatic_punctuation --max_alternatives 3 ../audio/sample_1.mp3
```

Also there is a REST-like API for non-streaming speech recognition:

```
$ python3 recognize_rest.py -r 16000 -c 1 -e MPEG_AUDIO ../audio/sample_2.mp3
```

To get description of all command-line parameters:

```
$ python3 recognize.py --help
```

### Basic synthesis examples

To run basic speech synthesis and save result to wav:

```
$ python3 synthesize_stream.py -r 48000 -e LINEAR16 "И мысли тоже тяжелые и медлительные, падают неторопливо и редко одна за другой, точно песчинки в разленившихся песочных часах." output_1.wav
```

You can also specify alternative stress with `0` sign after a vowel:

```
$ python3 synthesize_stream.py -r 48000 -e LINEAR16 "За0мок - замо0к." output_2.wav
```

Feel free to use arabic numerals and named entities:

```
$ python3 synthesize_stream.py -r 48000 -e LINEAR16 "Газета Times, 03 января 2009 года - Канцлер на грани ради второго спасения банков." output_3.wav
```

For now, `LINEAR16` does not support samples rates other than 48kHz. Use `RAW_OPUS` (`opuslib` required) to specify different sample rates:

```
$ python3 synthesize_stream.py -r 16000 -e RAW_OPUS "Привет, мир." output_4.wav
```

To get description of all command-line parameters:

```
$ python3 synthesize_stream.py --help
```

### Advanced examples

Use `RAW_OPUS` for both synthesis and recognition to save bandwidth:

```
$ python3 synthesize_stream.py -r 48000 -e RAW_OPUS "Газета Times, 03 января 2009 года - Канцлер на грани ради второго спасения банков." output.raw_opus
```

```
$ python3 recognize_stream.py -e RAW_OPUS -r 48000 -c 1 output.raw_opus
```

Run streaming speech recognition with interim results and disabled voice activity detection (VAD):

```
$ python3 recognize_stream.py -e MPEG_AUDIO --interim_results --do_not_perform_vad -r 16000 -c 1 ../audio/sample_1.mp3
```

Use audio from microphone (requires `PyAudio`):

```
$ python3 recognize_stream.py -e LINEAR16 --interim_results -r 16000 -c 1 --silence_duration_threshold 0.3 pyaudio:
```

Specify longer silence timeout for voice activity detection:

```
$ python3 recognize_stream.py -e LINEAR16 --interim_results -r 16000 -c 1 --silence_duration_threshold 1.2 pyaudio:
```

Return just the first recognized utterance and halt:

```
$ python3 recognize_stream.py -e LINEAR16 --interim_results -r 16000 -c 1 --single_utterance pyaudio:
```

Synthesize and play audio directly through your speakers at the same time.
This is faster than saving to file because of streaming (requires `PyAudio`):

```
$ python3 synthesize_stream.py -r 48000 -e LINEAR16 "И мысли тоже тяжелые и медлительные, падают неторопливо и редко одна за другой, точно песчинки в разленившихся песочных часах." pyaudio:
```

## Generate Protobuf and gRPC definitions (optional)

In case of API changes (`*.proto` files in `apis` directory),
you may regenerate Protobuf and gRPC definitions by simply running the following script
(no need to re-clone the whole repo):

```
$ ./sh/generate_protobuf.sh
```
