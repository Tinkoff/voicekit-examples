#!/usr/bin/env python3

import sys
sys.path.append("..")

from tinkoff.cloud.tts.v1 import tts_pb2_grpc, tts_pb2
from auth import authorization_metadata
import grpc
import os

endpoint = os.environ.get("VOICEKIT_ENDPOINT") or "tts.tinkoff.ru:443"
api_key = os.environ["VOICEKIT_API_KEY"]
secret_key = os.environ["VOICEKIT_SECRET_KEY"]

sample_rate = 16000


def build_request():
    return tts_pb2.SynthesizeSpeechRequest(
        input=tts_pb2.SynthesisInput(
            text="И мысли тоже тяжелые и медлительные, падают неторопливо и редко одна за другой, точно песчинки "
                 "в разленившихся песочных часах."
        ),
        audio_config=tts_pb2.AudioConfig(
            audio_encoding=tts_pb2.RAW_OPUS,
            sample_rate_hertz=sample_rate,
        ),
    )


stub = tts_pb2_grpc.TextToSpeechStub(grpc.secure_channel(endpoint, grpc.ssl_channel_credentials()))
request = build_request()
metadata = authorization_metadata(api_key, secret_key, "tinkoff.cloud.tts")
response = stub.Synthesize(request, metadata=metadata)

with open("synthesized.ogg", "wb") as f:
    f.write(response.audio_content)
