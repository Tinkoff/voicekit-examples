const fs = require('fs');
const {createSttClient, forEachRecognitionResult, checkWavFormat} = require('./common.js');
const {buildRecognizeCommand} = require('./args.js');
const stream = require('stream');
const wav = require('wav');
const concat = require('concat-stream');
const util = require('util');


const argv = buildRecognizeCommand().parse();

function main() {
    const sttClient = createSttClient();
    const reader = fs.createReadStream(argv.file);
    reader.on('error', (err) => {
        console.error(err);
        process.exit(1);
    });
    const concatStream = concat((buf) => {
        const unaryCall = sttClient.Recognize({
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
            audio: {
                content: buf,
            }
        }, (error, response) => {
            if (error != null) {
                console.error(error);
            } else {
                console.log(util.inspect(response, false, null, true));
            }
        });
        unaryCall.on('metadata', (metadata) => console.log("Initial response metadata", metadata));
        unaryCall.on('status', (status) => {
            console.log("Call ended, status", status);
            sttClient.close();
        });
    });

    if (argv.file.endsWith(".wav")) {
        const wavReader = wav.Reader();
        wavReader.on('format', (format) => {
            if (!checkWavFormat(format, argv)) {
                process.exit(1);
            } else {
                wavReader.pipe(concatStream);
            }
        });
        reader.pipe(wavReader);
    } else {
        reader.pipe(concatStream);
    }
}

main();
