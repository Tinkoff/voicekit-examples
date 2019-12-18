const auth = require('./auth.js');
const protoLoader = require('@grpc/proto-loader');
const grpcLibrary = require('grpc');

const packageDefinition = protoLoader.loadSync(
    [
        __dirname + '/../apis/tinkoff/cloud/stt/v1/stt.proto',
        __dirname + '/../apis/tinkoff/cloud/tts/v1/tts.proto',
    ],
    {
        keepCase: false,
        longs: String,
        enums: String,
        defaults: true,
        oneofs: true
    });
const sttProto = grpcLibrary.loadPackageDefinition(packageDefinition).tinkoff.cloud.stt.v1;
const ttsProto = grpcLibrary.loadPackageDefinition(packageDefinition).tinkoff.cloud.tts.v1;

function createAuthCredentials() {
    const apiKey = process.env.VOICEKIT_API_KEY;
    const secretKey = process.env.VOICEKIT_SECRET_KEY;
    if (apiKey == null || secretKey == null) {
        console.error("No VOICEKIT_API_KEY or VOICEKIT_SECRET_KEY environment variable defined");
        process.exit(1);
    }

    const channelCredentials = grpcLibrary.credentials.createSsl();
    const callCredentials = grpcLibrary.credentials.createFromMetadataGenerator(
        auth.jwtMetadataGenerator(apiKey, secretKey, "test_issuer", "test_subject"));

    return grpcLibrary.credentials.combineChannelCredentials(channelCredentials, callCredentials);
}

function createSttClient() {
    return new sttProto.SpeechToText("stt.tinkoff.ru:443", createAuthCredentials());
}

function createTtsClient() {
    return new ttsProto.TextToSpeech("tts.tinkoff.ru:443", createAuthCredentials());
}

function checkWavFormat(format, argv) {
    const formatTags = {
        'LINEAR16': 0x0001,
        'ALAW': 0x0006,
        'MULAW': 0x0007,
    };
    const bitDepth = {
        'LINEAR16': 16,
        'ALAW': 8,
        'MULAW': 8,
    };

    if (format.endianness !== 'LE') {
        console.error(format.endianness, 'endian not supported');
        return false;
    }
    if (format.channels !== argv.numChannels) {
        console.error("Specified", argv.numChannels, "channels, but wav header reports", format.channels, "channels");
        return false;
    }
    if (format.sampleRate !== argv.rate) {
        console.error("Specified sample rate", argv.rate, "Hz, but wav header reports", format.sampleRate, "Hz");
        return false;
    }
    if (format.audioFormat !== formatTags[argv.encoding]) {
        console.error("Specified encoding", argv.encoding, "but wav header reports format tag", format.audioFormat);
        return false;
    }
    if (format.bitDepth !== bitDepth[argv.encoding]) {
        console.error("Specified encoding", argv.encoding, "but wav header reports bit depth", format.bitDepth);
        return false;
    }
    return true;
}

module.exports = {
    createSttClient,
    createTtsClient,
    checkWavFormat,
};
