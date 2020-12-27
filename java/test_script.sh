set -e

./gradlew run --args="recognize -e=MPEG_AUDIO -c=1 -r=16000 -p=../audio/sample_1.mp3"

./gradlew run --args="streaming-recognize -e=MPEG_AUDIO -c=1 -r=16000 --max-alternatives=3 --disable-automatic-punctuation=true -p=../audio/sample_1.mp3"

./gradlew run --args="streaming-recognize -e=MPEG_AUDIO -r=16000 -c=1 --enable-interim-results=true -p=../audio/sample_1.mp3"

./gradlew run --args="streaming-recognize -e=MPEG_AUDIO -r=16000 -c=1 --enable-interim-results=true --silence-duration-threshold=1.2 -p=../audio/sample_1.mp3"

./gradlew run --args="streaming-recognize -e=MPEG_AUDIO -r=16000 -c=1 --enable-interim-results=true --single-utterance=true -p=../audio/sample_1.mp3"

./gradlew run --args="synthesize -t='И мысли тоже тяжелые и медлительные, падают неторопливо и редко одна за другой, точно песчинки в разленившихся песочных часах.' -o=./output_1.wav"

./gradlew run --args="synthesize -t='За0мок - замо0к.' -o=./output_2.wav"

./gradlew run --args="synthesize -t='Газета Times, 03 января 2009 года - Канцлер на грани ради второго спасения банков.' -o=./output_3.wav"

./gradlew run --args="synthesize -t='Привет! Меня зовут Алена.' -o=./output_3.wav -v=alyona"