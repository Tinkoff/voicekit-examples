#! /usr/bin/env bash

python3 -m pip install -U grpcio_tools

python3 -m grpc_tools.protoc \
    -I../apis/ -I../third_party/googleapis \
    --python_out=. --grpc_python_out=. \
    ../apis/tinkoff/cloud/stt/v1/stt.proto ../apis/tinkoff/cloud/tts/v1/tts.proto


find tinkoff -type d -exec touch {}/__init__.py \;
