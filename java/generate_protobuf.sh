set -e


mkdir src/main/grpc -p
PROTOC_OPTIONS="-I../third_party/googleapis/ -I../apis/ --java_out=./src/main/java --grpc_out=./src/main/java"

if [[ -z "$PATH_TO_JAVA_PLUGIN" ]]; then
    PATH_TO_JAVA_PLUGIN="$HOME/VoiceKit/protoc-gen-grpc-java-1.32.1-linux-x86_64.exe"
fi
if ! test -f "$PATH_TO_JAVA_PLUGIN"; then
        echo "$PATH_TO_JAVA_PLUGIN is not are file"
        exit 1
fi

protoc $PROTOC_OPTIONS --plugin=protoc-gen-grpc=$PATH_TO_JAVA_PLUGIN ../apis/tinkoff/cloud/stt/v1/*.proto
protoc $PROTOC_OPTIONS --plugin=protoc-gen-grpc=$PATH_TO_JAVA_PLUGIN ../apis/tinkoff/cloud/tts/v1/*.proto