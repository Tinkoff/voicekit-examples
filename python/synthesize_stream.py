#! /usr/bin/env python3
from audio import audio_open_write
from auth import authorization_metadata
from common import (
    BaseSynthesisParser,
    make_channel,
    build_synthesis_request,
)
from tinkoff.cloud.tts.v1 import tts_pb2_grpc, tts_pb2


def main():
    args = BaseSynthesisParser().parse_args()
    if args.encoding == tts_pb2.LINEAR16 and args.rate != 48000:
        raise ValueError("LINEAR16 supports only 48kHz for now, use RAW_OPUS")

    with audio_open_write(args.output_file, args.encoding, args.rate) as audio_writer:
        stub = tts_pb2_grpc.TextToSpeechStub(make_channel(args))
        request = build_synthesis_request(args)
        metadata = authorization_metadata(args.api_key, args.secret_key, "tinkoff.cloud.tts")
        responses = stub.StreamingSynthesize(request, metadata=metadata)
        for stream_response in responses:
            audio_writer.write(stream_response.audio_chunk)


if __name__ == "__main__":
    main()
