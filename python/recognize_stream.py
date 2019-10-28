#! /usr/bin/env python3
import logging
from audio import audio_open_read
from tinkoff.cloud.stt.v1 import stt_pb2_grpc, stt_pb2
from auth import authorization_metadata
from common import build_first_streaming_recognition_request, make_channel, \
        print_streaming_recognition_responses, StreamingRecognitionParser

logger = logging.getLogger()


def generate_requests(args, reader):
    try:
        yield build_first_streaming_recognition_request(args)
        while True:
            data = reader.read()
            if not data:
                break
            request = stt_pb2.StreamingRecognizeRequest()
            request.audio_content = data
            yield request
    except:
        logger.exception("Got exception in generate_requests")
        raise


def main():
    args = StreamingRecognitionParser().parse_args()

    with audio_open_read(args.audio_file, args.encoding, args.rate, args.num_channels, args.chunk_size,
                         args.pyaudio_max_seconds) as reader:
        stub = stt_pb2_grpc.SpeechToTextStub(make_channel(args))
        metadata = authorization_metadata(args.api_key, args.secret_key, "tinkoff.cloud.stt")
        responses = stub.StreamingRecognize(generate_requests(args, reader), metadata=metadata)
        print_streaming_recognition_responses(responses)


if __name__ == "__main__":
    main()
