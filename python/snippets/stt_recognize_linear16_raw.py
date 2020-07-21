#!/usr/bin/env python3

import sys
sys.path.append("..")

from tinkoff.cloud.stt.v1 import stt_pb2_grpc, stt_pb2
from auth import authorization_metadata
import grpc
import os

endpoint = os.environ.get("VOICEKIT_ENDPOINT") or "stt.tinkoff.ru:443"
api_key = os.environ["VOICEKIT_API_KEY"]
secret_key = os.environ["VOICEKIT_SECRET_KEY"]

def build_request():
    request = stt_pb2.RecognizeRequest()
    with open("../../audio/sample_3.s16", "rb") as f:
        request.audio.content = f.read()
    request.config.encoding = stt_pb2.AudioEncoding.LINEAR16
    request.config.sample_rate_hertz = 16000 # Not stored at raw ".s16" file
    request.config.num_channels = 1 # Not stored at raw ".s16" file
    return request

def print_recognition_response(response):
    for result in response.results:
        print("Channel", result.channel)
        print("Phrase start:", result.start_time.ToTimedelta())
        print("Phrase end:  ", result.end_time.ToTimedelta())
        for alternative in result.alternatives:
            print('"' + alternative.transcript + '"')
        print("----------------------------")

stub = stt_pb2_grpc.SpeechToTextStub(grpc.secure_channel(endpoint, grpc.ssl_channel_credentials()))
metadata = authorization_metadata(api_key, secret_key, "tinkoff.cloud.stt")
response = stub.Recognize(build_request(), metadata=metadata)
print_recognition_response(response)
