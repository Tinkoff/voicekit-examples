# C# examples

You will need to install `.net core 2.1`.

### Basic recognition examples

Run basic (non-streaming) speech recognition example:

```
$ dotnet run -- recognize -e MPEG_AUDIO -c 1 -r 16000 -p ../audio/sample_1.mp3
```

To disable automatic punctuation and get up to 3 recognition alternatives:

```
$ dotnet run -- streaming-recognize -e MPEG_AUDIO -c 1 -r 16000 --max-alternatives 3 --disable-automatic-punctuation true -p ../audio/sample_1.mp3
```

Run streaming speech recognition with interim results:

```
$ dotnet run -- streaming-recognize -e MPEG_AUDIO -r 16000 -c 1 --enable-interim-results true -p ../audio/sample_1.mp3
```

Specify longer silence timeout for voice activity detection (you will probably need longer audio to actually see the difference):

```
$ dotnet run -- streaming-recognize -e MPEG_AUDIO -r 16000 -c 1 --enable-interim-results true --silence-duration-threshold 1.2 -p ../audio/sample_1.mp3
```

Return just the first recognized utterance and halt (you will probably need longer audio to actually see the difference):

```
$ dotnet run -- streaming-recognize -e MPEG_AUDIO -r 16000 -c 1 --enable-interim-results true --single-utterance true -p ../audio/sample_1.mp3
```

### Basic synthesis examples

To run basic speech synthesis and save result to wav:

```
$ dotnet run -- synthesize -t "И мысли тоже тяжелые и медлительные, падают неторопливо и редко одна за другой, точно песчинки в разленившихся песочных часах." -o ./output_1.wav
```

You can also specify alternative stress with `0` sign after a vowel:

```
$ dotnet run -- synthesize -t "За0мок - замо0к." -o ./output_2.wav
```

Feel free to use arabic numerals and named entities:

```
$ dotnet run -- synthesize -t "Газета Times, 03 января 2009 года - Канцлер на грани ради второго спасения банков." -o ./output_3.wav
```

## Generate Protobuf and gRPC definitions (optional)
It works only on ubuntu, if you want generate from another OC, change path to csharp-plugin.

In case of API changes (`*.proto` files in `apis` directory),
you may regenerate Protobuf and gRPC definitions by simply running the following script
(no need to re-clone the whole repo):

```
$ ./generate_protobuf.sh
```
