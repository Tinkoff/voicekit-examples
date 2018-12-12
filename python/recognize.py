#! /usr/bin/env python3
from apis import stt_pb2_grpc
from auth import authorization_metadata
from common import build_recognition_request, make_channel, \
        print_recognition_response, RecognitionParser


def main():
    args = RecognitionParser().parse_args()
    if args.encoding == "RAW_OPUS":
        raise ValueError("RAW_OPUS encoding is not supported by this script")
    stub = stt_pb2_grpc.SpeechToTextStub(make_channel(args.host, args.port))
    metadata = authorization_metadata(args.api_key, args.secret_key, "tinkoff.cloud.stt")
    response = stub.Recognize(build_recognition_request(args), metadata=metadata)
    print_recognition_response(response)


if __name__ == "__main__":
    main()

