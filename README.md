# Tinkoff Speech API Examples

### Usage

#### Clone this repo

```
$ git clone --recursive https://github.com/TinkoffCreditSystems/tinkoff-speech-api-examples.git
$ cd tinkoff-speech-api-examples
```

#### Install requirements

```
$ pip3 install -r requirements.txt
```

#### Generate protobuf and grpc definitions for your language (Python):

```
$ ./sh/generate_protobuf.sh
```

#### Setup environment

Set `STT_TEST_API_KEY` and `STT_TEST_SECRET_KEY` environment variables to your API key and secret key to authenticate on server:

```bash
export STT_TEST_SECRET_KEY="SECRET_KEY"
export STT_TEST_API_KEY="API_KEY"
```

#### Run examples

```
$ ./sh/recognize.sh
```

```
$ ./sh/recognize_rest.sh
```

```
$ ./sh/recognize_stream_file.sh audio/sample_1.mp3
```

One should install sox library to recognize recording from a microphone (`apt 
install sox` / `brew install sox`):

```
$ ./sh/recognize_stream_mic.sh
```

```
$ ./sh/synthesize_stream.sh
```

You may get scope tinkoff.cloud.tts is not supported error if your API key does not
support speech synthesis.

### Note on endpoint format

Use `stt.tinkoff.ru:443` for speech recognition and `tts.tinkoff.ru:443` for speech synthesis.
