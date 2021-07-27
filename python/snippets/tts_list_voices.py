#!/usr/bin/env python3

import sys
sys.path.append("..")

from tinkoff.cloud.tts.v1 import tts_pb2_grpc, tts_pb2
from auth import authorization_metadata
import grpc
import os

endpoint = os.environ.get("VOICEKIT_ENDPOINT") or "api.tinkoff.ai:443"
api_key = os.environ["VOICEKIT_API_KEY"]
secret_key = os.environ["VOICEKIT_SECRET_KEY"]


stub = tts_pb2_grpc.TextToSpeechStub(grpc.secure_channel(endpoint, grpc.ssl_channel_credentials()))
request = tts_pb2.ListVoicesRequest()
metadata = authorization_metadata(api_key, secret_key, "tinkoff.cloud.tts")
response = stub.ListVoices(request, metadata=metadata)

print("Allowed voices:")
for voice in sorted(response.voices, key=lambda voice: voice.name):
    print(f"- {voice.name}")
