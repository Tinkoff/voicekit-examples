#! /bin/bash

source "./sh/env.sh"
sox -q -d -t s16 - rate 8k channels 1 | \
    python3 -m recognize_stream --host stt.tinkoff.ru --port 443 \
    --rate 8000 --num_channels 1 --encoding LINEAR16 \
    --chunk_size 4096 --api_key $STT_TEST_API_KEY --secret_key $STT_TEST_SECRET_KEY
