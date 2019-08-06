import numpy as np

from apis.tts_pb2_grpc import TextToSpeechStub
from auth import authorization_metadata
from common import (
    BaseSynthesisParser,
    make_channel,
    build_synthesis_request,
    save_synthesis_wav,
    generate_utterances,
)


OPUS_MAX_FRAME_SIZE = 5760


def main():
    args = BaseSynthesisParser().parse_args()
    if args.encoding == "LINEAR16" and args.rate != 48000:
        raise ValueError("LINEAR16 supports only 48kHz for now, use RAW_OPUS")
    stub = TextToSpeechStub(make_channel(args.host, args.port))
    for i, utterance in enumerate(generate_utterances(args.text_file)):
        request = build_synthesis_request(args, utterance)
        metadata = authorization_metadata(args.api_key, args.secret_key, "tinkoff.cloud.tts")
        responses = stub.StreamingSynthesize(request, metadata=metadata)
        print("Started streaming response for {}.wav".format(i))
        audio_chunks = []
        if args.encoding == "RAW_OPUS":
            import opuslib
            decoder = opuslib.Decoder(args.rate, 1)
            for stream_response in responses:
                pcm_data = decoder.decode(stream_response.audio_chunk, OPUS_MAX_FRAME_SIZE, 0)
                np_chunk = np.frombuffer(pcm_data, dtype=np.int16)
                audio_chunks.append(np_chunk)
        elif args.encoding == "LINEAR16":
            for stream_response in responses:
                audio_chunk = stream_response.audio_chunk
                np_chunk = np.frombuffer(audio_chunk, dtype=np.int16)
                audio_chunks.append(np_chunk)
        audio_array = np.concatenate(audio_chunks)
        save_synthesis_wav(audio_array.tobytes(), f"{i}.wav", args.rate)


if __name__ == "__main__":
    main()
