# Tinkoff VoiceKit Examples

https://voicekit.tinkoff.ru

## Usage

#### Clone this repo

```
$ git clone --recursive https://github.com/TinkoffCreditSystems/voicekit-examples.git
$ cd voicekit-examples
```

#### Setup environment

Set `VOICEKIT_API_KEY` and `VOICEKIT_SECRET_KEY` environment variables to your API key and secret key to authenticate
your requests to VoiceKit:

```bash
export VOICEKIT_API_KEY="Your API key"
export VOICEKIT_SECRET_KEY="Your secret key"
```

You may get scope `tinkoff.cloud.tts` is not supported error if your API key does not
support speech synthesis. Write us a letter at https://voicekit.tinkoff.ru to enable
speech synthesis for you API key.

#### Language specific instructions

Follow language specific instructions in the related folder in repository root. E.g. for Python scripts, open
`python/README.md`

## Note on endpoint format

Use `stt.tinkoff.ru:443` for speech recognition and `tts.tinkoff.ru:443` for speech synthesis. Unencrypted endpoints (with port `80`) are not avaialable.
