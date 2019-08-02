# Tinkoff Speech API Examples

## Running examples

0. Clone this repo

```
$ git clone --recursive https://github.com/TinkoffCreditSystems/tinkoff-speech-api-examples.git
$ cd tinkoff-speech-api-examples
```

1. Install requirements

```
$ pip3 install -r requirements.txt
```

2. Generate protobuf and grpc definitions for your language (Python in this example):

```
$ ./sh/generate_protobuf.sh
```

3. Run examples

```
$ ./sh/recognize.sh
```

```
$ ./sh/recognize_rest.sh
```

```
$ ./sh/recognize_stream_file.sh audio/sample_1.mp3
```

The following example needs sox (`apt install sox` / `brew install sox`):

```
$ ./sh/recognize_stream_mic.sh
```
