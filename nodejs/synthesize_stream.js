const fs = require('fs');
const {createTtsClient} = require('./common.js');
const {buildStreamingSynthesizeCommand} = require('./args.js');
const stream = require('stream');
const wav = require('wav');
const util = require('util');


const argv = buildStreamingSynthesizeCommand().parse();

function main() {
    if (argv.encoding === 'LINEAR16' && argv.rate !== 48000) {
        console.error("Only 48 kHz sampling rate is supported for LINEAR16 for now");
        process.exit(1);
    }
    const ttsClient = createTtsClient();
    const ttsStreamingCall = ttsClient.StreamingSynthesize({
        input: {
            text: argv.inputText,
        },
        audioConfig: {
            audioEncoding: argv.encoding,
            sampleRateHertz: argv.rate,
        }
    });

    ttsStreamingCall.on('metadata', (metadata) => console.log("Initial response metadata", metadata));
    ttsStreamingCall.on('status', (status) => {
        console.log("Call ended, status", status);
        ttsClient.close();
    });
    ttsStreamingCall.on('error', (error) => console.error("Error", error));
    let startedStreaming = false;
    ttsStreamingCall.on('data', (response) => {
        if (!startedStreaming) {
            console.log("Started streaming back audio chunks");
            startedStreaming = true;
        }
    });

    let opusDecoder = null;
    if (argv.encoding === 'RAW_OPUS') {
        const opus = require('node-opus');
        opusDecoder = new opus.OpusEncoder(argv.rate);
    }
    const transformStream = new stream.Transform({
        writableObjectMode: true,
        transform(chunk, encoding, callback) {
            let pcm_data;
            if (opusDecoder != null) {
                const OPUS_MAX_FRAME_SIZE = 5760;
                pcm_data = opusDecoder.decode(chunk.audioChunk, OPUS_MAX_FRAME_SIZE);
            } else {
                pcm_data = chunk.audioChunk;
            }
            callback(null, pcm_data);
        }
    });

    const wavFileWriter = new wav.FileWriter(argv.outputFile, {
        channels: 1,
        sampleRate: argv.rate,
        bitDepth: 16,
    });
    ttsStreamingCall.pipe(transformStream).pipe(wavFileWriter);
}

main();
