# JAVA examples

### Basic recognition examples
Use `gradlew.bat` instead of `./gradlew` on windows 

Run basic (non-streaming) speech recognition example:

```
$ ./gradlew run --args="recognize -e=MPEG_AUDIO -c=1 -r=16000 -p=../audio/sample_1.mp3"
```

To disable automatic punctuation and get up to 3 recognition alternatives:

```
$ ./gradlew run --args="streaming-recognize -e=MPEG_AUDIO -c=1 -r=16000 --max-alternatives=3 --disable-automatic-punctuation=true -p=../audio/sample_1.mp3"
```

Run streaming speech recognition with interim results:

```
$ ./gradlew run --args="streaming-recognize -e=MPEG_AUDIO -r=16000 -c=1 --enable-interim-results=true -p=../audio/sample_1.mp3"
```

Specify longer silence timeout for voice activity detection (you will probably need longer audio to actually see the difference):

```
$ ./gradlew run --args="streaming-recognize -e=MPEG_AUDIO -r=16000 -c=1 --enable-interim-results=true --silence-duration-threshold=1.2 -p=../audio/sample_1.mp3"
```

Return just the first recognized utterance and halt (you will probably need longer audio to actually see the difference):

```
$ ./gradlew run --args="streaming-recognize -e=MPEG_AUDIO -r=16000 -c=1 --enable-interim-results=true --single-utterance=true -p=../audio/sample_1.mp3"
```

Use audio from microphone
```
$ ./gradlew run --args="microphone"
```

### Basic synthesis examples

To run basic speech synthesis and save result to wav:

```
$ ./gradlew run --args="synthesize -t='И мысли тоже тяжелые и медлительные, падают неторопливо и редко одна за другой, точно песчинки в разленившихся песочных часах.' -o=./output_1.wav"
```

You can also specify alternative stress with `0` sign after a vowel:

```
$ ./gradlew run --args="synthesize -t='За0мок - замо0к.' -o=./output_2.wav"
```

Feel free to use arabic numerals and named entities:

```
$ ./gradlew run --args="synthesize -t='Газета Times, 03 января 2009 года - Канцлер на грани ради второго спасения банков.' -o=./output_3.wav"
```

Specify voice: alyona, maxim, flirt

```
$ ./gradlew run --args="synthesize -t='Привет! Меня зовут Алена.' -o=./output_4.wav -v=alyona"
```

## Generate Protobuf and gRPC definitions (optional)
Download and setup path to [java-plugin](https://mvnrepository.com/artifact/io.grpc/protoc-gen-grpc-java).

In case of API changes (`*.proto` files in `apis` directory),
you may regenerate Protobuf and gRPC definitions by simply running the following script
(no need to re-clone the whole repo):

```
$ ./generate_protobuf.sh
```
