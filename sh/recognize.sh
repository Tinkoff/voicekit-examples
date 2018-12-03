#! /usr/bin/env bash

source "./sh/env.sh"
python3 -m recognize --host stt.tinkoff.ru --port 443 \
    --api_key $STT_TEST_API_KEY --secret_key $STT_TEST_SECRET_KEY \
    --rate 16000 --num_channels 1 --encoding MPEG_AUDIO --audio_file audio/sample_1.mp3
