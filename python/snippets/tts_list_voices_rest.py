#!/usr/bin/env python3

import sys
sys.path.append("..")

from auth import authorization_metadata
import os
import requests

endpoint = os.environ.get("VOICEKIT_ENDPOINT") or "api.tinkoff.ai:443"
api_key = os.environ["VOICEKIT_API_KEY"]
secret_key = os.environ["VOICEKIT_SECRET_KEY"]


metadata = authorization_metadata(api_key, secret_key, "tinkoff.cloud.tts", type=dict)
response = requests.get(f"http{'s' if endpoint.endswith('443') else ''}://{endpoint}/v1/tts:list_voices", headers=metadata)

if response.status_code != 200:
    print(f"REST failed with HTTP code {response.status_code}\nHeaders: {response.headers}\nBody: {response.text}")
else:
    response = response.json()
    print("Allowed voices:")
    for voice in sorted(response["voices"], key=lambda voice: voice["name"]):
        print(f"- {voice['name']}")
