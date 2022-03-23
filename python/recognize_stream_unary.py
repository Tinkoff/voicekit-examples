#! /usr/bin/env python3
import logging
from audio import audio_open_read
from tinkoff.cloud.stt.v1 import stt_pb2_grpc, stt_pb2
from auth import authorization_metadata
from common import build_first_streaming_unary_recognition_request, make_channel, \
        print_recognition_response, BaseRecognitionParser

logger = logging.getLogger()


def generate_requests(args, reader):
    try:
        yield build_first_streaming_unary_recognition_request(args)
        while True:
            data = reader.read()
            if not data:
                break
            request = stt_pb2.StreamingUnaryRecognizeRequest()
            request.audio_content = data
            yield request
    except:
        logger.exception("Got exception in generate_requests")
        raise


def main():
    args = BaseRecognitionParser().parse_args()

    with audio_open_read(args.audio_file, args.encoding, args.rate, args.num_channels, args.chunk_size,
                         args.pyaudio_max_seconds) as reader:
        stub = stt_pb2_grpc.SpeechToTextStub(make_channel(args))
        metadata = authorization_metadata(args.api_key, args.secret_key, "tinkoff.cloud.stt")
        response = stub.StreamingUnaryRecognize(generate_requests(args, reader), metadata=metadata)
        print_recognition_response(response)


if __name__ == "__main__":
    main()
