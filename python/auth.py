import base64
import copy
import hmac
import json
from time import time

TEN_MINUTES = 600  # seconds


def generate_jwt(api_key, secret_key, payload, expiration_time=TEN_MINUTES):
    header = {
        "alg": "HS256",
        "typ": "JWT",
        "kid": api_key
    }
    payload_copy = copy.deepcopy(payload)
    current_timestamp = int(time())
    payload_copy["exp"] = current_timestamp + expiration_time
    # payload_copy["iat"] = current_timestamp
    # payload_copy["nbf"] = current_timestamp
    payload_bytes = json.dumps(payload_copy, separators=(',', ':')).encode("utf-8")
    header_bytes = json.dumps(header, separators=(',', ':')).encode("utf-8")

    data = (base64.urlsafe_b64encode(header_bytes).strip(b'=') + b"." +
            base64.urlsafe_b64encode(payload_bytes).strip(b'='))

    signature = hmac.new(base64.urlsafe_b64decode(secret_key), msg=data, digestmod="sha256")
    jwt = data + b"." + base64.urlsafe_b64encode(signature.digest()).strip(b'=')
    return jwt.decode("utf-8")


def authorization_metadata(api_key, secret_key, scope, type=list):
    auth_payload = {
        "iss": "test_issuer",
        "sub": "test_user",
        "aud": scope
    }

    metadata = [
        ("authorization", "Bearer " + generate_jwt(api_key, secret_key, auth_payload))
    ]
    return type(metadata)
