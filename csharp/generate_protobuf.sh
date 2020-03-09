#! /usr/bin/env bash

dotnet add package Google.Api.CommonProtos --version 1.7.0
dotnet add package Google.Protobuf --version 3.11.2
dotnet add package Grpc --version 2.25.0
dotnet add package Grpc.Tools --version 2.25.0


if [[ -z "$PATH_TO_CSHARP_PLUGIN" ]]; then
    PATH_TO_CSHARP_PLUGIN="$HOME/.nuget/packages/grpc.tools/2.25.0/tools/linux_x86/grpc_csharp_plugin"
fi
if ! test -f "$PATH_TO_CSHARP_PLUGIN"; then
        echo "$PATH_TO_CSHARP_PLUGIN is not are file"
        exit 1
fi

mkdir grpc -p
PROTOC_OPTIONS="-I../third_party/googleapis/ -I../apis/ --csharp_out=./grpc --grpc_out=./grpc"

protoc $PROTOC_OPTIONS --plugin=protoc-gen-grpc=$PATH_TO_CSHARP_PLUGIN  ../apis/tinkoff/cloud/stt/v1/*.proto
protoc $PROTOC_OPTIONS --plugin=protoc-gen-grpc=$PATH_TO_CSHARP_PLUGIN  ../apis/tinkoff/cloud/tts/v1/*.proto