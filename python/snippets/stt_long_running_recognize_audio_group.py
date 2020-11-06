#!/usr/bin/env python3

import sys

sys.path.append("..")

from tinkoff.cloud.stt.v1 import stt_pb2_grpc, stt_pb2
from auth import authorization_metadata
from tinkoff.cloud.longrunning.v1 import longrunning_pb2_grpc, longrunning_pb2
from tinkoff.cloud.longrunning.v1.longrunning_pb2 import OperationState, FAILED, ENQUEUED, DONE, PROCESSING
from datetime import datetime
import grpc
import os
from os.path import isfile, join

endpoint = os.environ.get("VOICEKIT_ENDPOINT") or "api.tinkoff.ai:443"
api_key = os.environ["VOICEKIT_API_KEY"]
secret_key = os.environ["VOICEKIT_SECRET_KEY"]


def build_recognize_request(file_path, group_name):
    request = stt_pb2.LongRunningRecognizeRequest()
    # Note: setting the group name here allows us filtering by group name in WatchOperations
    request.group = group_name
    with open(file_path, "rb") as f:
        request.audio.content = f.read()
    request.config.encoding = stt_pb2.AudioEncoding.LINEAR16
    request.config.sample_rate_hertz = 48000  # Not stored at raw ".s16" file
    request.config.num_channels = 1  # Not stored at raw ".s16" file
    return request


def build_watch_operations_request(group_name):
    request = longrunning_pb2.WatchOperationsRequest()
    request.filter.exact_group = group_name
    # Note: listen_for_updates is set to False by default.
    # Setting it to True here is required for update notifications to be sent.
    request.listen_for_updates = True
    return request


def build_get_operation_request(id):
    request = longrunning_pb2.GetOperationRequest()
    request.id = id
    return request


def print_longrunning_operations(operations):
    for operation in operations:
        print(f"[{operation.id}] {get_recognition_state_description(operation)}")
    print("============================")


def get_recognition_state_description(operation):
    if operation.state == DONE:
        response = stt_pb2.RecognizeResponse()
        operation.response.Unpack(response)

        return " ".join([result.alternatives[0].transcript for result in response.results])
    if operation.state == FAILED:
        return operation.error

    return OperationState.Name(operation.state)


group_name = datetime.now().strftime("test-group-%Y-%m-%d, %H:%M:%S")
audio_folder = "../../audio/sample_group"

# Send audio files for recognition
stt_stub = stt_pb2_grpc.SpeechToTextStub(grpc.secure_channel(endpoint, grpc.ssl_channel_credentials()))
stt_metadata = authorization_metadata(api_key, secret_key, "tinkoff.cloud.stt")
created_operations = 0
for test_file in os.listdir(audio_folder):
    file_path = join(audio_folder, test_file)
    stt_stub.LongRunningRecognize(build_recognize_request(file_path, group_name), metadata=stt_metadata)
    created_operations += 1

# Wait for results by calling WatchOperations
operations_stub = longrunning_pb2_grpc.OperationsStub(grpc.secure_channel(endpoint, grpc.ssl_channel_credentials()))
operations_metadata = authorization_metadata(api_key, secret_key, "tinkoff.cloud.longrunning")


def count_finished_operations(operations):
    return sum([int(operation.state == DONE) for operation in operations])


print(f"Watching operations in group '{group_name}'")
responses = operations_stub.WatchOperations(build_watch_operations_request(group_name), metadata=operations_metadata)
finished_operations = 0
for response in responses:
    if response.HasField("initial_state"):
        finished_operations += count_finished_operations(response.initial_state.operations)
        print("WatchOperations. Initial state:")
        print_longrunning_operations(response.initial_state.operations)
    elif response.HasField("init_finished"):
        print("WatchOperations. Init finished.")
    else:
        assert response.HasField("update")
        finished_operations += count_finished_operations(response.update.operations)
        print("WatchOperations. Update:")
        print_longrunning_operations(response.update.operations)

    if finished_operations == created_operations:
        break
print("Done.")
