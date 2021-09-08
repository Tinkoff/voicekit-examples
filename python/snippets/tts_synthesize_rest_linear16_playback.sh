#!/usr/bin/env bash

if [ -z "${VOICEKIT_API_KEY+x}" ]; then
  echo "${0}: error: specify VOICEKIT_API_KEY environment variable" >&2
  exit 1
elif [ -z "${VOICEKIT_API_KEY}" ]; then
  echo "${0}: error: VOICEKIT_API_KEY environment variable can't be empty" >&2
  exit 1
fi

if [ -z "${VOICEKIT_SECRET_KEY+x}" ]; then
  echo "${0}: error: specify VOICEKIT_SECRET_KEY environment variable" >&2
  exit 1
elif [ -z "${VOICEKIT_SECRET_KEY}" ]; then
  echo "${0}: error: VOICEKIT_SECRET_KEY environment variable can't be empty" >&2
  exit 1
fi

TEN_MINUTES=600
JWT=$(
  ./gen_jwt.sh --api_key "${VOICEKIT_API_KEY}" \
               --secret_key "${VOICEKIT_SECRET_KEY}" \
               --scope tinkoff.cloud.tts \
               --exp $(("$(date +%s)" + "${TEN_MINUTES}"))
)

SAMPLE_RATE=22050

request="$(
  jq --null-input \
    --arg input_text 'Мы подсчитали, что шанс «один на миллион» выпадает в девяти случаях из десяти.' \
    --arg audio_encoding 'LINEAR16' \
    --arg sample_rate "${SAMPLE_RATE}" \
    --arg voice_name 'alyona:funny' \
    '
    .input.text = $input_text |
    .audioConfig.audioEncoding = $audio_encoding |
    .audioConfig.sampleRateHertz = ($sample_rate | tonumber) |
    .voice.name = $voice_name
    '
)"

## Result:
#request='{
#  "input": {
#    "text": "Мы подсчитали, что шанс «один на миллион» выпадает в девяти случаях из десяти."
#  },
#  "audioConfig": {
#    "audioEncoding": "LINEAR16",
#    "sampleRateHertz": 22050
#  },
#  "voice": {
#    "name": "alyona:funny"
#  }
#}'

curl --header "Content-Type: application/json" \
     --header "Authorization: Bearer ${JWT}" \
     --request POST \
     --data "${request}" \
      https://api.tinkoff.ai:443/v1/tts:synthesize | jq --raw-output .audio_content | base64 -d | play --type s16 --rate "${SAMPLE_RATE}" --channels 1 -
