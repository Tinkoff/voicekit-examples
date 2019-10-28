#! /usr/bin/env python3
import requests
from audio import audio_open_read
from auth import authorization_metadata
from common import build_recognition_request, print_recognition_response, BaseRecognitionParser

from tinkoff.cloud.stt.v1 import stt_pb2


def main():
    args = BaseRecognitionParser().parse_args()

    if args.encoding == stt_pb2.RAW_OPUS:
        raise ValueError("RAW_OPUS encoding is not supported by this script")
    with audio_open_read(args.audio_file, args.encoding, args.rate, args.num_channels, args.chunk_size,
                         args.pyaudio_max_seconds) as reader:
        metadata = authorization_metadata(args.api_key, args.secret_key, "tinkoff.cloud.stt", type=dict)
        request = build_recognition_request(args, reader, type="json")
        response = requests.post("http{}://{}/v1/stt:recognize".format("s" if args.endpoint.endswith("443") else "",
                                                                       args.endpoint), json=request, headers=metadata)

        if response.status_code != 200:
            print("REST failed with HTTP code {}\nHeaders: {}\nBody: {}".format(
                response.status_code, response.headers, response.text))
            return
        response = response.json()
        print_recognition_response(response)


if __name__ == "__main__":
    main()
