#!/usr/bin/env python3

import sys
sys.path.append("..")

from tinkoff.cloud.tts.v1 import tts_pb2_grpc, tts_pb2
from auth import authorization_metadata
import grpc
import os
import wave

endpoint = os.environ.get("VOICEKIT_ENDPOINT") or "tts.tinkoff.ru:443"
api_key = os.environ["VOICEKIT_API_KEY"]
secret_key = os.environ["VOICEKIT_SECRET_KEY"]

sample_rate = 48000

def build_request():
    return tts_pb2.SynthesizeSpeechRequest(
        input=tts_pb2.SynthesisInput(text="И с каждым днем мечта о киберпространстве становилась все более призрачной."),
        audio_config=tts_pb2.AudioConfig(
            audio_encoding=tts_pb2.LINEAR16,
            sample_rate_hertz=sample_rate,
        ),
    )

with wave.open("synthesised.wav", "wb") as f:
    f.setframerate(sample_rate)
    f.setnchannels(1)
    f.setsampwidth(2)

    stub = tts_pb2_grpc.TextToSpeechStub(grpc.secure_channel(endpoint, grpc.ssl_channel_credentials()))
    request = build_request()
    metadata = authorization_metadata(api_key, secret_key, "tinkoff.cloud.tts")
    responses = stub.StreamingSynthesize(request, metadata=metadata)
    for key, value in responses.initial_metadata():
        if key == "x-audio-num-samples":
            print("Estimated audio duration is " + str(int(value)/sample_rate) + " seconds")
            break
    for stream_response in responses:
        f.writeframes(stream_response.audio_chunk)
