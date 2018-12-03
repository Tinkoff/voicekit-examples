#! /usr/bin/env bash

python3 -m grpc_tools.protoc \
    -Ipython -Ithird_party/googleapis \
    --python_out=python --grpc_python_out=python \
    python/apis/stt.proto python/apis/tts.proto
