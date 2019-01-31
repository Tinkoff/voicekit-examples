# Tinkoff Speech API Examples

### Usage

#### Clone this repo

```
$ git clone --recurse-submodules git@github.com:TinkoffCreditSystems/tinkoff-speech-api-examples.git
$ cd tinkoff-speech-api-examples
```

#### Install requirements

```
$ pip3 install -r requirements.txt
```

#### Generate protobuf and grpc definitions for your language (Python in this 
example):

```
$ ./sh/generate_protobuf.sh
```

#### Setup environment

One need API keys to authenticate on server.

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
