#!/usr/bin/env python3

import sys

sys.path.append("..")

from tinkoff.cloud.stt.v1 import stt_pb2_grpc, stt_pb2
from auth import authorization_metadata
from tinkoff.cloud.longrunning.v1 import longrunning_pb2_grpc, longrunning_pb2
from tinkoff.cloud.longrunning.v1.longrunning_pb2 import OperationState, FAILED, ENQUEUED, DONE, PROCESSING
import grpc
import os
import time

endpoint = os.environ.get("VOICEKIT_ENDPOINT") or "api.tinkoff.ai:443"
api_key = os.environ["VOICEKIT_API_KEY"]
secret_key = os.environ["VOICEKIT_SECRET_KEY"]


def build_recognize_request():
    request = stt_pb2.LongRunningRecognizeRequest()
    with open("../../audio/sample_3.s16", "rb") as f:
        request.audio.content = f.read()
    request.config.encoding = stt_pb2.AudioEncoding.LINEAR16
    request.config.sample_rate_hertz = 16000  # Not stored at raw ".s16" file
    request.config.num_channels = 1  # Not stored at raw ".s16" file
    return request


def build_get_operation_request(id):
    request = longrunning_pb2.GetOperationRequest()
    request.id = id
    return request


def print_longrunning_operation(operation):
    print("Operation id:", operation.id)
    print("State:", OperationState.Name(operation.state))
    if operation.state == DONE:
        response = stt_pb2.RecognizeResponse()
        operation.response.Unpack(response)
        print_recognition_response(response)
    if operation.state == FAILED:
        print("Error:", operation.error)
    print("============================")


def print_recognition_response(response):
    for result in response.results:
        print("Channel", result.channel)
        print("Phrase start:", result.start_time.ToTimedelta())
        print("Phrase end:  ", result.end_time.ToTimedelta())
        for alternative in result.alternatives:
            print('"' + alternative.transcript + '"')
        print("----------------------------")


# Send audio for recognition
stt_stub = stt_pb2_grpc.SpeechToTextStub(grpc.secure_channel(endpoint, grpc.ssl_channel_credentials()))
stt_metadata = authorization_metadata(api_key, secret_key, "tinkoff.cloud.stt")
operation = stt_stub.LongRunningRecognize(build_recognize_request(), metadata=stt_metadata)
print_longrunning_operation(operation)

# Wait for results by checking operation state periodically
operations_stub = longrunning_pb2_grpc.OperationsStub(grpc.secure_channel(endpoint, grpc.ssl_channel_credentials()))
operations_metadata = authorization_metadata(api_key, secret_key, "tinkoff.cloud.longrunning")

while operation.state != FAILED and operation.state != DONE:
    time.sleep(1)
    operation = operations_stub.GetOperation(build_get_operation_request(operation.id), metadata=operations_metadata)
    print_longrunning_operation(operation)
