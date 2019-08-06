#!/usr/bin/env bash

source "./sh/env.sh"
python3 -m synthesize_stream --host stt.tinkoff.ru --port 443 \
    --text_file ./input.txt --encoding LINEAR16 --rate 48000 \
    --api_key ${STT_TEST_API_KEY} --secret_key ${STT_TEST_SECRET_KEY}
