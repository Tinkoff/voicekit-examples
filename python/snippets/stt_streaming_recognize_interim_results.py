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
    request.streaming_config.interim_results_config.enable_interim_results = True
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

def format_time_stamp(timestamp):
    return "{:>02d}:{:>02d}:{:>02d}.{:>03d}".format(timestamp.seconds//(60*60), (timestamp.seconds//60)%60, timestamp.seconds%60, timestamp.microseconds//1000)

def time_range(recognition_result):
    return "[" + format_time_stamp(recognition_result.start_time.ToTimedelta()) + " .. " + format_time_stamp(recognition_result.end_time.ToTimedelta()) + "]"

def print_streaming_recognition_responses(responses):
    inside_phrase = False
    for response in responses:
        for result in response.results:
            if not inside_phrase:
                print("[Phrase begin]")
                inside_phrase = True
            assert(len(result.recognition_result.alternatives) == 1) # Handle carefully at real service
            if result.is_final:
                print("Final result:   " + time_range(result.recognition_result) + " \"" + result.recognition_result.alternatives[0].transcript + "\"")
                print("[Phrase end]")
                inside_phrase = False
            else:
                print("Interim result: " + time_range(result.recognition_result) + " \"" + result.recognition_result.alternatives[0].transcript + "\"")

stub = stt_pb2_grpc.SpeechToTextStub(grpc.secure_channel(endpoint, grpc.ssl_channel_credentials()))
metadata = authorization_metadata(api_key, secret_key, "tinkoff.cloud.stt")
responses = stub.StreamingRecognize(generate_requests(), metadata=metadata)
print_streaming_recognition_responses(responses)
