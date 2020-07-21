#!/usr/bin/env python3

import sys
sys.path.append("..")

from tinkoff.cloud.stt.v1 import stt_pb2_grpc, stt_pb2
from auth import authorization_metadata
import grpc
import os
import wave
import opuslib

endpoint = os.environ.get("VOICEKIT_ENDPOINT") or "stt.tinkoff.ru:443"
api_key = os.environ["VOICEKIT_API_KEY"]
secret_key = os.environ["VOICEKIT_SECRET_KEY"]

def build_first_request(sample_rate_hertz, num_channels):
    request = stt_pb2.StreamingRecognizeRequest()
    request.streaming_config.config.encoding = stt_pb2.AudioEncoding.RAW_OPUS
    request.streaming_config.config.sample_rate_hertz = sample_rate_hertz
    request.streaming_config.config.num_channels = num_channels
    return request

def frame_rate_is_valid(frame_rate):
    return frame_rate in [8000, 12000, 16000, 24000, 48000]

def get_padded_frame_size(frame_samples, frame_rate):
    for duration_half_msec in [5, 10, 20, 40, 80, 120]:
        padded_samples = frame_rate//2000*duration_half_msec
        if frame_samples <= padded_samples:
            return padded_samples
    raise("Unexpected frame samples")

def generate_requests():
    try:
        with wave.open("../../audio/sample_3.wav") as f:
            frame_rate_is_valid(f.getframerate())
            yield build_first_request(f.getframerate(), f.getnchannels())
            frame_samples = f.getframerate()//1000*60 # 60ms
            opus_encoder = opuslib.Encoder(f.getframerate(), f.getnchannels(), opuslib.APPLICATION_AUDIO)
            for data in iter(lambda:f.readframes(frame_samples), b''): # Send 60ms at a time
                if len(data) < frame_samples*2: # Padding last frame to closest allowed frame size
                    data = data.ljust(get_padded_frame_size(frame_samples, f.getframerate())*2, b'\0')
                request = stt_pb2.StreamingRecognizeRequest()
                request.audio_content = opus_encoder.encode(data, len(data) >> 1)
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
