#!/usr/bin/env python3

import sys
sys.path.append("..")

from tinkoff.cloud.tts.v1 import tts_pb2_grpc, tts_pb2
from auth import authorization_metadata
import grpc
import os
import pyaudio
import audioop

endpoint = os.environ.get("VOICEKIT_ENDPOINT") or "api.tinkoff.ai:443"
api_key = os.environ["VOICEKIT_API_KEY"]
secret_key = os.environ["VOICEKIT_SECRET_KEY"]

sample_rate = 8000


def build_request():
    return tts_pb2.SynthesizeSpeechRequest(
        input=tts_pb2.SynthesisInput(
            text="Все ищут во мне тайну. А во мне нет тайны, во мне все просто и ясно. Никаких тайн. Я привык с жизнью "
                 "встречаться прямо. Не отличая большого от малого."
        ),
        audio_config=tts_pb2.AudioConfig(
            audio_encoding=tts_pb2.ALAW,
            sample_rate_hertz=sample_rate,
        ),
    )


pyaudio_lib = pyaudio.PyAudio()
f = pyaudio_lib.open(output=True, channels=1, format=pyaudio.paInt16, rate=sample_rate)

stub = tts_pb2_grpc.TextToSpeechStub(grpc.secure_channel(endpoint, grpc.ssl_channel_credentials()))
request = build_request()
metadata = authorization_metadata(api_key, secret_key, "tinkoff.cloud.tts")
responses = stub.StreamingSynthesize(request, metadata=metadata)
for key, value in responses.initial_metadata():
    if key == "x-audio-duration-seconds":
        print("Estimated audio duration is {:.2f} seconds".format(float(value)))
        break
for stream_response in responses:
    pcm_chunk = audioop.alaw2lin(stream_response.audio_chunk, 2)
    f.write(pcm_chunk)
f.stop_stream()
f.close()
