#!/usr/bin/env python3

import sys
sys.path.append("..")

from tinkoff.cloud.stt.v1 import stt_pb2_grpc, stt_pb2
from auth import authorization_metadata
import grpc
import os
import wave

endpoint = os.environ.get("VOICEKIT_ENDPOINT") or "stt.tinkoff.ru:443"
api_key = os.environ["VOICEKIT_API_KEY"]
secret_key = os.environ["VOICEKIT_SECRET_KEY"]

def build_first_request(sample_rate_hertz, num_channels):
    request = stt_pb2.StreamingRecognizeRequest()
    request.streaming_config.config.encoding = stt_pb2.AudioEncoding.LINEAR16
    request.streaming_config.config.sample_rate_hertz = sample_rate_hertz
    request.streaming_config.config.num_channels = num_channels
    request.streaming_config.config.vad_config.min_speech_duration = 1.0
    request.streaming_config.config.vad_config.max_speech_duration = 30.0
    request.streaming_config.config.vad_config.silence_duration_threshold = 3.0
    request.streaming_config.config.vad_config.silence_prob_threshold = 0.2
    return request

def generate_requests():
    try:
        with wave.open("../../audio/sample_3.wav") as f:
            yield build_first_request(f.getframerate(), f.getnchannels())
            frame_samples = f.getframerate()//10 # Send 100ms at a time
            for data in iter(lambda:f.readframes(frame_samples), b''):
                request = stt_pb2.StreamingRecognizeRequest()
                request.audio_content = data
                yield request
    except Exception as e:
        print("Got exception in generate_requests", e)
        raise

def print_streaming_recognition_responses(responses):
    for response in responses:
        for result in response.results:
            print("Channel", result.recognition_result.channel)
            print("Phrase start:", result.recognition_result.start_time.ToTimedelta())
            print("Phrase end:  ", result.recognition_result.end_time.ToTimedelta())
            for alternative in result.recognition_result.alternatives:
                print('"' + alternative.transcript + '"')
            print("------------------")

stub = stt_pb2_grpc.SpeechToTextStub(grpc.secure_channel(endpoint, grpc.ssl_channel_credentials()))
metadata = authorization_metadata(api_key, secret_key, "tinkoff.cloud.stt")
responses = stub.StreamingRecognize(generate_requests(), metadata=metadata)
print_streaming_recognition_responses(responses)
