#!/usr/bin/env python3

import sys
sys.path.append("..")

from tinkoff.cloud.stt.v1 import stt_pb2_grpc, stt_pb2
from auth import authorization_metadata
import grpc
import os
import wave

endpoint = os.environ.get("VOICEKIT_ENDPOINT") or "api.tinkoff.ai:443"
api_key = os.environ["VOICEKIT_API_KEY"]
secret_key = os.environ["VOICEKIT_SECRET_KEY"]

def build_first_request(sample_rate_hertz, num_channels, context):
    request = stt_pb2.StreamingRecognizeRequest()
    request.streaming_config.config.encoding = stt_pb2.AudioEncoding.LINEAR16
    request.streaming_config.config.sample_rate_hertz = sample_rate_hertz
    request.streaming_config.config.num_channels = num_channels
    # ВАЖНО!
    # Не рекомендуется:
    # - добавление слов короче 5 символов
    # - выставление score в слишком большое значение
    request.streaming_config.config.speech_contexts.append(context)
    return request

def generate_requests(context):
    try:
        with wave.open("../../audio/numbers.wav") as f:
            yield build_first_request(f.getframerate(), f.getnchannels(), context)
            frame_samples = f.getframerate()//10 # Send 100ms at a time
            for data in iter(lambda:f.readframes(frame_samples), b''):
                request = stt_pb2.StreamingRecognizeRequest()
                request.audio_content = data
                yield request
            # Sending 1 second of silence
            for i in range(10):
                request = stt_pb2.StreamingRecognizeRequest()
                request.audio_content = bytes(frame_samples)
                yield request
    except Exception as e:
        print("Got exception in generate_requests", e)
        raise

def generate_repeated_requests(times_repeated):
    context = stt_pb2.SpeechContext(speech_context_dictionary_id = "numbers")
    null_context = stt_pb2.SpeechContext(phrases = [])
    # Контекст может быть указан несколько раз во время стриминговой сессии
    # В этом примере чередуется добавление контекста и его сброс
    # Стоит отметить, что для сброса контекста нужно переслать SpeechContext с пустым списком фраз
    for i in range(times_repeated):
        for request in generate_requests(context if (i % 2 == 1) else null_context):
            yield request


def print_streaming_recognition_responses(responses):
    for response in responses:
        for result in response.results:
            print("Channel", result.recognition_result.channel)
            print("Phrase start:", result.recognition_result.start_time.ToTimedelta())
            print("Phrase end:  ", result.recognition_result.end_time.ToTimedelta())
            for alternative in result.recognition_result.alternatives:
                print('"' + alternative.transcript + '"')
            print("------------------")

#stub = stt_pb2_grpc.SpeechToTextStub(grpc.secure_channel(endpoint, grpc.ssl_channel_credentials()))
stub = stt_pb2_grpc.SpeechToTextStub(grpc.insecure_channel(endpoint))
metadata = authorization_metadata(api_key, secret_key, "tinkoff.cloud.stt")
responses = stub.StreamingRecognize(generate_repeated_requests(3), metadata=metadata)
print_streaming_recognition_responses(responses)
