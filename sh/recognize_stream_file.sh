#! /bin/bash

source "./sh/env.sh"
cat $1 | \
    python3 -m recognize_stream --host stt.tinkoff.ru --port 443 \
    --rate 16000 --num_channels 1 --encoding MPEG_AUDIO \
    --chunk_size 8192 --api_key $STT_TEST_API_KEY --secret_key $STT_TEST_SECRET_KEY
