import argparse
import os
import warnings
from functools import cached_property

import grpc
from google.protobuf.json_format import MessageToDict

from tinkoff.cloud.stt.v1 import stt_pb2
from tinkoff.cloud.tts.v1 import tts_pb2


def set_recognition_config(config, args):
    config.encoding = args.encoding
    config.sample_rate_hertz = args.rate
    config.num_channels = args.num_channels
    config.max_alternatives = args.max_alternatives
    if args.do_not_perform_vad:
        config.do_not_perform_vad = args.do_not_perform_vad
    else:
        config.vad_config.silence_duration_threshold = args.silence_duration_threshold
    config.language_code = args.language_code
    config.enable_automatic_punctuation = not args.disable_automatic_punctuation
    config.enable_denormalization = not args.disable_denormalization
    config.profanity_filter = not args.disable_profanity_filter


def build_recognition_request(args, audio_reader, type="pb"):
    request = stt_pb2.RecognizeRequest()
    request.audio.content = audio_reader.read_all()
    set_recognition_config(request.config, args)
    return request if type != "json" else MessageToDict(request)


def build_first_streaming_recognition_request(args):
    request = stt_pb2.StreamingRecognizeRequest()
    set_recognition_config(request.streaming_config.config, args)
    request.streaming_config.interim_results_config.enable_interim_results = args.interim_results
    request.streaming_config.single_utterance = args.single_utterance
    return request

def build_first_streaming_unary_recognition_request(args):
    request = stt_pb2.StreamingUnaryRecognizeRequest()
    set_recognition_config(request.config, args)
    return request

def make_channel(args):
    target = args.endpoint
    if target.endswith("443"):
        if args.ca_file:
            with open(args.ca_file, "rb") as pem:
                creds = grpc.ssl_channel_credentials(pem.read())
        else:
            creds = grpc.ssl_channel_credentials()
        return grpc.secure_channel(target, creds)
    else:
        return grpc.insecure_channel(target)


def print_recognition_response(response):
    if not isinstance(response, dict):
        # https://developers.google.com/protocol-buffers/docs/proto3#json
        response = MessageToDict(response,
                                 including_default_value_fields=True,
                                 preserving_proto_field_name=True)
    for result in response["results"]:
        print("Channel", result["channel"])
        print("Phrase start", result["start_time"])
        print("Phrase end", result["end_time"])
        for alternative in result["alternatives"]:
            print("Transcription", alternative["transcript"])
            print("Confidence", alternative["confidence"])
        print("----------------------------")


def print_streaming_recognition_responses(responses):
    for response in responses:
        for result in response.results:
            print("Channel", result.recognition_result.channel)
            print("Phrase start", result.recognition_result.start_time.ToTimedelta())
            print("Phrase end", result.recognition_result.end_time.ToTimedelta())
            print("Is final", result.is_final)
            for alternative in result.recognition_result.alternatives:
                print("Transcription", alternative.transcript)
                print("Confidence", alternative.confidence)
            print("------------------")


class ProtobufEnumChoices:
    def __init__(self, protobuf_enum_type, string_choices):
        self._protobuf_enum_type = protobuf_enum_type
        for string_choice in string_choices:
            if string_choice not in protobuf_enum_type.keys():
                raise ValueError("{} not in {}".format(string_choice, protobuf_enum_type.keys()))
        self._string_choices = string_choices
        self._int_chioces = list(map(self, self._string_choices))

    def __call__(self, string_choice: str):
        return self._protobuf_enum_type.Value(string_choice)

    def __iter__(self):
        return iter(self._string_choices)

    def __contains__(self, item):
        return item in self._int_chioces


class CommonParser(argparse.ArgumentParser):
    def _get_key(self, new_env_name, deprecated_env_name, command_line_parameter):
        value = os.getenv(new_env_name, None)
        if value is None:
            value = os.getenv(deprecated_env_name, None)
            if value is not None:
                warnings.warn("Using deprecated {} environment variable, consider migrating to {}".format(
                    deprecated_env_name, new_env_name
                ))
        return value

    @cached_property
    def _default_api_key(self):
        return self._get_key("VOICEKIT_API_KEY", "STT_TEST_API_KEY", "api_key")

    @cached_property
    def _default_secret_key(self):
        return self._get_key("VOICEKIT_SECRET_KEY", "STT_TEST_SECRET_KEY", "secret_key")

    @property
    def _default_endpoint(self):
        raise NotImplementedError("override in derived classes")

    def __init__(self):
        super().__init__()
        self.add_argument("--endpoint", type=str, default=self._default_endpoint,
                          help="API endpoint, a secure channel will be used if a port ends with 443 (443, 8443, etc). "
                          "Default will use api.tinkoff.ai:443 for both speech recognition and synthesis.")
        if self._default_api_key is None:
            self.add_argument("--api_key", type=str, required=True, help="API key for JWT authentication.")
        else:
            self.add_argument("--api_key", type=str, default=self._default_api_key,
                              help="API key for JWT authentication.")
        if self._default_secret_key is None:
            self.add_argument("--secret_key", type=str, required=True,
                              help="Secret key for HMAC-based JWT authentication.")
        else:
            self.add_argument("--secret_key", type=str, default=self._default_secret_key,
                              help="Secret key for HMAC-based JWT authentication.")
        self.add_argument("--ca_file", type=str, default=None,
                          help="Custom root certificates file to use with non-default endpoint.")


class BaseRecognitionParser(CommonParser):
    @property
    def _default_endpoint(self):
        return "api.tinkoff.ai:443"

    def __init__(self):
        super().__init__()
        encoding = ProtobufEnumChoices(stt_pb2.AudioEncoding,
                                       ["MPEG_AUDIO", "LINEAR16", "ALAW", "MULAW", "RAW_OPUS", "ADTS_AAC"])
        self.add_argument("-r", "--rate", type=int, required=True, help="Audio sampling rate.")
        self.add_argument("-c", "--num_channels", type=int, required=True, help="Number of audio channels.")
        self.add_argument("-e", "--encoding", type=encoding, required=True, help="Audio encoding.", choices=encoding)
        self.add_argument("--max_alternatives", type=int, default=1, help="Number of speech recognition alternatives "
                          "to return.")
        self.add_argument("--do_not_perform_vad", action='store_true',
                          help="Specify this to disable voice activity detection. All audio is processed "
                          "as though it were a single utterance.")
        self.add_argument("--silence_duration_threshold", type=float, default=0.6,
                          help="Silence threshold in seconds for VAD to assume the current utterance is ended and "
                          "the next utterance shall begin.")
        self.add_argument("--language_code", type=str, choices=["ru-RU"], default="ru-RU",
                          help="Language for speech recognition.")
        self.add_argument("--disable_automatic_punctuation", action="store_true",
                          help="Specify this to disable automatic punctuation in recognition results.")
        self.add_argument("--disable_denormalization", action="store_true",
                          help="Specify this to disable automatic text denormalization.")
        self.add_argument("--disable_profanity_filter", action="store_true",
                          help="Specify this to disable profanity filter.")
        self.add_argument("--chunk_size", type=int, default=1024, help="Chunk size for streaming")
        self.add_argument("--pyaudio_max_seconds", type=float, default=None, help="Maximum length of pyaudio "
                          "recording in seconds.")
        self.add_argument("audio_file", type=str, help="Audio file to recognize or 'pyaudio:' to use stream from mic.")


class StreamingRecognitionParser(BaseRecognitionParser):
    def __init__(self):
        super().__init__()
        self.add_argument("--interim_results", action="store_true", help="Yield interim results")
        self.add_argument("--single_utterance", action="store_true", help="Recognize only first utterance")


class BaseSynthesisParser(CommonParser):
    @property
    def _default_endpoint(self):
        return "api.tinkoff.ai:443"

    def __init__(self):
        super().__init__()
        encoding = ProtobufEnumChoices(tts_pb2.AudioEncoding,
                                       ["LINEAR16", "RAW_OPUS"])
        self.add_argument("-r", "--rate", type=int, required=True, help="Audio sample rate",
                          choices=[8000, 16000, 24000, 48000])
        self.add_argument("-e", "--encoding", type=encoding, required=True, help="Audio encoding", choices=encoding)
        self.add_argument("--ssml", action='store_true', help="Enable SSML")
        self.add_argument("--voice", type=str, help="Voice name")
        self.add_argument("input_text", type=str, help="Input text to synthesize")
        self.add_argument("output_file", type=str, help="Output wav to save or 'pyaudio:' to play with speakers.")


def build_synthesis_request(args, *, type="pb"):
    if args.ssml:
        input = tts_pb2.SynthesisInput(ssml=args.input_text)
    else:
        input = tts_pb2.SynthesisInput(text=args.input_text)
    audio_config = tts_pb2.AudioConfig(
        audio_encoding=args.encoding,
        sample_rate_hertz=args.rate,
    )
    voice = tts_pb2.VoiceSelectionParams(name=args.voice)
    request = tts_pb2.SynthesizeSpeechRequest(
        input=input,
        audio_config=audio_config,
        voice=voice,
    )
    return request if type != "json" else MessageToDict(request)
