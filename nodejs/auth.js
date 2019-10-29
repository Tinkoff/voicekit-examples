const crypto = require('crypto');
const grpcLibrary = require('grpc');

function base64Encode(string) {
    return Buffer.from(string).toString('base64')
        .replace(/\+/g, '-')  // Convert '+' to '-'
        .replace(/\//g, '_')  // Convert '/' to '_'
        .replace(/=+$/, '');  // Remove ending '='
}

function base64Decode(base64) {
    return Buffer.from(base64, 'base64');
}

function generateJwt(apiKey, secretKey, payload) {
    const expirationTime = 600;
    const header = {
        "alg": "HS256",
        "typ": "JWT",
        "kid": apiKey
    };

    payload['exp'] = new Date() / 1000 + expirationTime;
    const headerBytes = JSON.stringify(header);
    const payloadBytes = JSON.stringify(payload);
    const data = base64Encode(headerBytes) + '.' + base64Encode(payloadBytes);

    const hmac = crypto.createHmac('sha256', base64Decode(secretKey)).update(data,'utf8').digest();
    const signature = base64Encode(hmac);

    return data + '.' + signature;
}

function jwtMetadataGenerator(apiKey, secretKey, issuer, subject) {
    return (params, callback) => {
        const authPayload = {
            "iss": issuer,
            "sub": subject,
            "aud": params['service_url'].split('/').pop()
        };

        const token = generateJwt(apiKey, secretKey, authPayload);
        const metadata = new grpcLibrary.Metadata();
        metadata.set('authorization', 'Bearer ' + token);
        callback(null, metadata);
    }
}

module.exports = {
    generateJwt,
    jwtMetadataGenerator,
};
