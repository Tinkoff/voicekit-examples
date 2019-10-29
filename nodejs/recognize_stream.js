const fs = require('fs');
const {createSttClient, checkWavFormat} = require('./common.js');
const {buildStreamingRecognizeCommand} = require('./args.js');
const stream = require('stream');
const wav = require('wav');
const util = require('util');


const argv = buildStreamingRecognizeCommand().parse();

function main() {
    const sttClient = createSttClient();
    const sttStreamingCall = sttClient.StreamingRecognize();

    sttStreamingCall.on('metadata', (metadata) => console.log("Initial response metadata", metadata));
    sttStreamingCall.on('status', (status) => {
        console.log("Call ended, status", status);
        sttClient.close();
    });
    sttStreamingCall.on('error', (error) => console.error("Error", error));
    sttStreamingCall.on('data', (response) => console.log(util.inspect(response, false, null, true)));

    const configMessage = {
        streamingConfig: {
            config: {
                encoding: argv.encoding,
                sampleRateHertz: argv.rate,
                languageCode: argv.languageCode,
                maxAlternatives: argv.maxAlternatives,
                numChannels: argv.numChannels,
                enableAutomaticPunctuation: argv.automaticPunctuation,
                doNotPerformVad: (argv.performVad ? undefined : true),
                vadConfig: (!argv.performVad ? undefined : {
                    silenceDurationThreshold: argv.silenceDurationThreshold
                }),
            },
            interimResultsConfig: {
                enableInterimResults: argv.interimResults,
            },
            singleUtterance: argv.singleUtterance,
        }
    };

    sttStreamingCall.write(configMessage);

    const reader = fs.createReadStream(argv.file);
    const transformStream = new stream.Transform({
        readableObjectMode: true,
        transform(chunk, encoding, callback) {
            callback(null, {audioContent: chunk});
        }
    });

    if (argv.file.endsWith(".wav")) {
        const wavReader = wav.Reader();
        wavReader.on('format', (format) => {
            if (!checkWavFormat(format, argv)) {
                sttStreamingCall.end();
                process.exit(1);
            } else {
                wavReader.pipe(transformStream).pipe(sttStreamingCall);
            }
        });
        reader.pipe(wavReader);
    } else {
        reader.pipe(transformStream).pipe(sttStreamingCall);
    }
}

main();
