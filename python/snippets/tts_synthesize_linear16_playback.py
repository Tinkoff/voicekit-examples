#!/usr/bin/env python3

import sys
sys.path.append("..")

from tinkoff.cloud.tts.v1 import tts_pb2_grpc, tts_pb2
from auth import authorization_metadata
import grpc
import os
import pyaudio

endpoint = os.environ.get("VOICEKIT_ENDPOINT") or "api.tinkoff.ai:443"
api_key = os.environ["VOICEKIT_API_KEY"]
secret_key = os.environ["VOICEKIT_SECRET_KEY"]

sample_rate = 48000


def build_request():
    return tts_pb2.SynthesizeSpeechRequest(
        input=tts_pb2.SynthesisInput(
            text="И мысли тоже тяжелые и медлительные, падают неторопливо и редко одна за другой, точно песчинки "
                 "в разленившихся песочных часах."
        ),
        audio_config=tts_pb2.AudioConfig(
            audio_encoding=tts_pb2.LINEAR16,
            sample_rate_hertz=sample_rate,
        ),
        voice=tts_pb2.VoiceSelectionParams(
            name="maxim",
        ),
    )

# increase this number if you are trying to synthesize really long text
ONE_HUNDRED_MEGABYTE_BUFFER = 100 * 1024 * 1024

stub = tts_pb2_grpc.TextToSpeechStub(
    grpc.secure_channel(
        endpoint,
        grpc.ssl_channel_credentials(),
        options=[
            ("grpc.max_message_length", ONE_HUNDRED_MEGABYTE_BUFFER),
            ("grpc.max_receive_message_length", ONE_HUNDRED_MEGABYTE_BUFFER),
        ],
    )
)
request = build_request()
metadata = authorization_metadata(api_key, secret_key, "tinkoff.cloud.tts")
response = stub.Synthesize(request, metadata=metadata)

pyaudio_lib = pyaudio.PyAudio()
f = pyaudio_lib.open(output=True, channels=1, format=pyaudio.paInt16, rate=sample_rate)
f.write(response.audio_content)
f.stop_stream()
f.close()
