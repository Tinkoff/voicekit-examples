#! /usr/bin/env bash

set -e

if ! [[ -x "$(command -v protoc-gen-go)" ]]; then
  go get -u google.golang.org/protobuf/cmd/protoc-gen-go
  go install google.golang.org/protobuf/cmd/protoc-gen-go

if ! [[ -x "$(command -v protoc-gen-go)" ]]; then
    echo "Failed to install google.golang.org/protobuf/cmd/protoc-gen-go, check your GOPATH"
    exit 1
  fi
fi


if ! [[ -x "$(command -v protoc-gen-go-grpc)" ]]; then
  go get -u google.golang.org/grpc/cmd/protoc-gen-go-grpc
  go install google.golang.org/grpc/cmd/protoc-gen-go-grpc

if ! [[ -x "$(command -v protoc-gen-go-grpc)" ]]; then
    echo "Failed to install google.golang.org/protobuf/cmd/protoc-gen-go, check your GOPATH"
    exit 1
  fi
fi



PROTOC_OPTS="-I../third_party/googleapis/ -I../apis/ --go_out=:temp --go-grpc_out=:temp"

mkdir -p temp/

protoc $PROTOC_OPTS ../apis/tinkoff/cloud/stt/v1/*.proto
protoc $PROTOC_OPTS ../apis/tinkoff/cloud/tts/v1/*.proto
protoc $PROTOC_OPTS ../apis/tinkoff/cloud/longrunning/v1/*.proto

rm -rf pkg/tinkoff

mv temp/github.com/Tinkoff/voicekit-examples/golang/pkg/* pkg

rm -rf temp/
