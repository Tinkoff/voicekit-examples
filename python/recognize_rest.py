#! /usr/bin/env python3
import requests
from auth import authorization_metadata
from common import build_recognition_request, make_channel, \
        print_recognition_response, RecognitionParser

def main():
    args = RecognitionParser().parse_args()

    metadata = authorization_metadata(args.api_key, args.secret_key, "tinkoff.cloud.stt", type=dict)
    request = build_recognition_request(args, type="json")
    response = requests.post("https://{}:{}/v1/stt:recognize".format(args.host, args.port),
                             json=request, headers=metadata)

    if response.status_code != 200:
        print("REST failed with HTTP code {}\nHeaders: {}\nBody: {}".format(
            response.status_code, response.headers, response.text))
        return
    response = response.json()
    print_recognition_response(response)


if __name__ == "__main__":
    main()
