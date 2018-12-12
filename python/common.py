import argparse
import grpc
from google.protobuf.json_format import MessageToDict

from apis import stt_pb2


def build_recognition_request(args, type="pb"):
    request = stt_pb2.RecognizeRequest()
    with open(args.audio_file, "rb") as fin:
        request.audio.content = fin.read()

    request.config.encoding = getattr(stt_pb2, args.encoding)
    request.config.sample_rate_hertz = args.rate
    request.config.num_channels = args.num_channels
    request.config.max_alternatives = args.max_alternatives
    request.config.do_not_perform_vad = args.do_not_perform_vad
    request.config.language_code = "ru-RU"
    return request if type != "json" else MessageToDict(request)


def build_first_streaming_recognition_request(args):
    request = stt_pb2.StreamingRecognizeRequest()
    request.streaming_config.config.encoding = getattr(stt_pb2, args.encoding)
    request.streaming_config.config.sample_rate_hertz = args.rate
    request.streaming_config.config.num_channels = args.num_channels
    request.streaming_config.interim_results_config.enable_interim_results = args.interim_results
    request.streaming_config.config.language_code = "ru-RU"
    return request


def make_channel(host, port):
    if port == 443:
        creds = grpc.ssl_channel_credentials()
        return grpc.secure_channel("{}:{}".format(host, port), creds)
    else:
        return grpc.insecure_channel("{}:{}".format(host, port))


def check_api_key(data):
    if not data:
        raise ValueError("API_KEY not provided or empty")
    return str(data)


def check_secret_key(data):
    if not data:
        raise ValueError("SECRET_KEY not provided or empty")
    return str(data)


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


class CommonParser(argparse.ArgumentParser):
    def __init__(self):
        super().__init__()
        self.add_argument("--host", type=str, required=True, help="Speech API endpoint host")
        self.add_argument("--port", type=int, required=True, help="Speech API endpoint port")
        self.add_argument("--api_key", type=check_api_key, required=True, help="API key")
        self.add_argument("--secret_key", type=check_secret_key, required=True, help="Secret key")


class BaseRecognitionParser(CommonParser):
    def __init__(self):
        super().__init__()
        self.add_argument("--rate", type=int, required=True, help="Audio sample rate")
        self.add_argument("--num_channels", type=int, required=True, help="Number of channels")
        self.add_argument("--encoding", type=str, required=True, help="Audio encoding",
                          choices=["MPEG_AUDIO", "LINEAR16", "LINEAR32F", "ALAW", "MULAW", "RAW_OPUS"])
        self.add_argument("--max_alternatives", type=int, default=1)


class RecognitionParser(BaseRecognitionParser):
    def __init__(self):
        super().__init__()
        self.add_argument("--do_not_perform_vad", action='store_true', help="Disable voice activity detection")
        self.add_argument("--audio_file", type=str, required=True,
                          help="File in specified format used for recognition")


class StreamingRecognitionParser(BaseRecognitionParser):
    def __init__(self):
        super().__init__()
        self.add_argument("--chunk_size", type=int, default=65536, help="Chunk size for streaming")
        self.add_argument("--interim_results", action="store_true", help="Yield interim results")

