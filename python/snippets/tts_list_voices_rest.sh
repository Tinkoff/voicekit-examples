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
JWT=$(./gen_jwt.sh --api_key ${VOICEKIT_API_KEY} --secret_key ${VOICEKIT_SECRET_KEY} --scope tinkoff.cloud.tts --exp $(("$(date +%s)" + "${TEN_MINUTES}")))

curl --header "Content-Type: application/json" \
     --header "Authorization: Bearer ${JWT}" \
     --request GET \
      https://tts.tinkoff.ru:443/v1/tts:list_voices | jq
