#!/usr/bin/env python3

import sys
sys.path.append("..")


from tinkoff.cloud.tts.v1 import tts_pb2
from google.protobuf.json_format import MessageToDict
from auth import authorization_metadata
import os
import pyaudio
import requests
import base64

endpoint = os.environ.get("VOICEKIT_ENDPOINT") or "tts.tinkoff.ru:443"
api_key = os.environ["VOICEKIT_API_KEY"]
secret_key = os.environ["VOICEKIT_SECRET_KEY"]

sample_rate = 48000


def build_request_from_pb():
    pb_request = tts_pb2.SynthesizeSpeechRequest(
        input=tts_pb2.SynthesisInput(
            text="И мысли тоже тяжелые и медлительные, падают неторопливо и редко одна за другой, точно песчинки "
                 "в разленившихся песочных часах.",
        ),
        audio_config=tts_pb2.AudioConfig(
            audio_encoding=tts_pb2.LINEAR16,
            sample_rate_hertz=sample_rate,
        ),
        voice=tts_pb2.VoiceSelectionParams(
            name="dorofeev:drama",
        ),
    )
    return MessageToDict(pb_request)


def build_request():
    return {
        "input": {
            "text": "И мысли тоже тяжелые и медлительные, падают неторопливо и редко одна за другой, точно песчинки "
                    "в разленившихся песочных часах.",
        },
        "audioConfig": {
            "audioEncoding": "LINEAR16",
            "sampleRateHertz": sample_rate,
        },
        "voice": {
            "name": "dorofeev:drama",
        }
    }


assert build_request() == build_request_from_pb()

pyaudio_lib = pyaudio.PyAudio()
f = pyaudio_lib.open(output=True, channels=1, format=pyaudio.paInt16, rate=sample_rate)

request = build_request()
metadata = authorization_metadata(api_key, secret_key, "tinkoff.cloud.tts", type=dict)
response = requests.post(f"http{'s' if endpoint.endswith('443') else ''}://{endpoint}/v1/tts:synthesize", json=request, headers=metadata)

if response.status_code != 200:
    print(f"REST failed with HTTP code {response.status_code}\nHeaders: {response.headers}\nBody: {response.text}")
else:
    response = response.json()
    f.write(base64.b64decode(response["audio_content"]))
