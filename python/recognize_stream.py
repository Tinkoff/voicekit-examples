#! /usr/bin/env python3
import sys
import struct
from apis import stt_pb2_grpc, stt_pb2
from auth import authorization_metadata
from common import build_first_streaming_recognition_request, make_channel, \
        print_recognition_response, StreamingRecognitionParser


def generate_requests(args):
    yield build_first_streaming_recognition_request(args)
    while True:
        if args.encoding != "RAW_OPUS":
            data = sys.stdin.buffer.read(args.chunk_size)
            if not data:
                break
        else:
            length_bytes = sys.stdin.buffer.read(4)
            if not length_bytes:
                break
            length = struct.unpack(">I", length_bytes)[0]
            data = sys.stdin.buffer.read(length)
        request = stt_pb2.StreamingRecognizeRequest()
        request.audio_content = data
        yield request


def main():
    args = StreamingRecognitionParser().parse_args()

    stub = stt_pb2_grpc.SpeechToTextStub(make_channel(args.host, args.port))
    metadata = authorization_metadata(args.api_key, args.secret_key, "tinkoff.cloud.stt")
    responses = stub.StreamingRecognize(generate_requests(args), metadata=metadata)
    for response in responses:
        for result in response.results:
            print("Channel", result.recognition_result.channel)
            print("Phrase start", result.recognition_result.start_time.ToTimedelta())
            print("Phrase end", result.recognition_result.end_time.ToTimedelta())
            print("Is final", result.is_final)
            for alternative in result.recognition_result.alternatives:
                print("Transcription", alternative.transcript)
                print("Confidence", alternative.confidence)
            print("------------------")


if __name__ == "__main__":
    main()
