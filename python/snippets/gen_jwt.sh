#!/usr/bin/env bash

function print_usage() {
  echo "Usage: ${0} --api_key API_KEY --secret_key SECRET_KEY --scope SCOPE --exp EXPIRATION_TIMESTAMP"
  echo "Options and arguments:"
  echo "--api_key API_KEY         Tinkoff VoiceKit API key"
  echo "--secret_key SECRET_KEY   Tinkoff VoiceKit secret key"
  echo "--scope SCOPE             scope/aud - token audience, service to authorize in"
  echo "--exp EXP                 JWT expiration timestamp"
  echo "--iss ISS                 token issuer, default is 'test_issuer'"
  echo "--sub SUB                 token subject, default is 'test_user'"
  echo ""
  echo "Visit voicekit.tinkoff.ru for more info."
}

# TODO: Support VOICEKIT_<NAME> environment variables
unset HELP API_KEY SECRET_KEY SCOPE EXP ISS SUB

ISS="test_issuer"
SUB="test_user"

while (($#)); do
  case "${1}" in
    -h | --help)
      HELP="y"
      ;;
    --api_key | --api_key=*)
      if [ "${1}" == "--api_key" ]; then shift; fi
      API_KEY="${1#--api_key=}"
      ;;
    --secret_key | --secret_key=*)
      if [ "${1}" == "--secret_key" ]; then shift; fi
      SECRET_KEY="${1#--secret_key=}"
      ;;
    --scope | --scope=*)
      if [ "${1}" == "--scope" ]; then shift; fi
      SCOPE="${1#--scope=}"
      ;;
    --exp | --exp=*)
      if [ "${1}" == "--exp" ]; then shift; fi
      EXP="${1#--exp=}"
      ;;
    --iss | --iss=*)
      if [ "${1}" == "--iss" ]; then shift; fi
      ISS="${1#--iss=}"
      ;;
    --sub | --sub=*)
      if [ "${1}" == "--sub" ]; then shift; fi
      SUB="${1#--sub=}"
      ;;
    *)
      echo "error: Unknown argument: ${1}" >&2
      echo "" >&2
      print_usage >&2
      exit 1
      ;;
  esac
  shift
done

if [ "${HELP}" == "y" ]; then
  echo "${0}: generate JWT token for Tinkoff VoiceKit"
  echo ""
  print_usage
  exit ${?}
fi

if [ -z "${API_KEY+x}" ]; then
  echo "${0}: error: specify API key" >&2
  echo "" >&2
  print_usage >&2
  exit 1
elif [ -z "${API_KEY}" ]; then
  echo "${0}: error: API key can't be empty" >&2
  echo "" >&2
  print_usage >&2
  exit 1
fi

if [ -z "${SECRET_KEY+x}" ]; then
  echo "${0}: error: specify secret key" >&2
  echo "" >&2
  print_usage >&2
  exit 1
elif [ -z "${SECRET_KEY}" ]; then
  echo "${0}: error: secret key can't be empty" >&2
  echo "" >&2
  print_usage >&2
  exit 1
fi

if [ -z "${SCOPE+x}" ]; then
  echo "${0}: error: specify scope" >&2
  echo "" >&2
  print_usage >&2
  exit 1
elif [ -z "${SCOPE}" ]; then
  echo "${0}: error: scope can't be empty" >&2
  echo "" >&2
  print_usage >&2
  exit 1
fi

if [ -z "${EXP+x}" ]; then
  echo "${0}: error: specify expiration timestamp" >&2
  echo "" >&2
  print_usage >&2
  exit 1
elif [ -z "${EXP}" ]; then
  echo "${0}: error: expiration timestamp can't be empty" >&2
  echo "" >&2
  print_usage >&2
  exit 1
fi

header=$(
  jq --null-input \
    --arg kid "${API_KEY}" \
    '
    .typ = "JWT" |
    .alg = "HS256" |
    .kid = $kid
    ' | base64 | tr -d '=\n' | tr '/+' '_-'
)

payload=$(
  jq --null-input \
    --arg iss "${ISS}" \
    --arg sub "${SUB}" \
    --arg aud "${SCOPE}" \
    --arg exp "${EXP}" \
    '
    .iss = $iss |
    .sub = $sub |
    .aud = $aud |
    .exp = ($exp | tonumber)
    ' | base64 | tr -d '=\n' | tr '/+' '_-'
)

signature=$(
  printf '%s' "${header}.${payload}" | openssl dgst -binary -sha256 -hmac "$(echo "${SECRET_KEY}" | tr '_-' '/+' | base64 -d)" | base64 | tr -d '=\n' | tr '/+' '_-'
)

echo "${header}.${payload}.${signature}"

exit ${?}
