#!/usr/bin/env python3

import sys
sys.path.append("..")

import os
import asyncio
import httpx
from auth import authorization_metadata

async def main():
    endpoint = os.environ.get("VOICEKIT_ENDPOINT") or "api.tinkoff.ai:443"
    api_key = os.environ["VOICEKIT_API_KEY"]
    secret_key = os.environ["VOICEKIT_SECRET_KEY"]

    async with httpx.AsyncClient(http2=True) as client:
        metadata = authorization_metadata(api_key, secret_key, "tinkoff.cloud.tts", type=dict)
        response = await client.get(f"http{'s' if endpoint.endswith('443') else ''}://{endpoint}/v1/tts:list_voices", headers=metadata)

        if response.status_code != 200:
            print(f"REST failed with HTTP code {response.status_code}\nHeaders: {response.headers}\nBody: {response.text}")
        else:
            response = response.json()
            print("Allowed voices:")
            for voice in sorted(response["voices"], key=lambda voice: voice["name"]):
                print(f"- {voice['name']}")


if __name__ == "__main__":
    asyncio.run(main())
