# Intro

Пример использования STT Tinkoff VoiceKit на Ruby

# Installation

```
bundle install
```

# Run examples

## REST

```
API_KEY=<your_api_key> SECRET_KEY=<your_secret_key> bundle exec ruby recognize_rest.rb -f <path/to/audio_file>
```

## GRPC

```
API_KEY=<your_api_key> SECRET_KEY=<your_secret_key> bundle exec ruby recognize_grpc.rb -f <path/to/audio_file>
```

# Options

```
bundle exec ruby recognize_rest.rb -h
Usage:  [options]
    -a [MAX_ALTERNATIVES],           Number of speech recognition alternatives to return
        --max-alternatives
    -v, --do-not-perform-vad         Specify this to disable voice activity detection. All audio is processed as though it were a single utterance
    -s [THR],                        Silence threshold in seconds for VAD to assume the current utterance is ended and the next utterance shall begin.
        --silence-duration-threshold
    -p                               Specify this to disable automatic punctuation in recognition results
        --disable-automatic-punctuation
    -f, --input-file [FILE]          Path to input file. Supported wav, mp3, flac, oga.
    -e, --endpoint [ENDPOINT]        Network endpoint
```


