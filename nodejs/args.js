const yargsLibrary = require('yargs');

function baseRecognitionOptions(yargs) {
    return yargs
        .strict(true)
        .positional('file', {
            describe: 'file to recognize',
            type: 'string',
        })
        .option('encoding', {
            alias: 'e',
            describe: 'Audio encoding.',
            type: 'string',
            choices: ['LINEAR16', 'ALAW', 'MULAW', 'MPEG_AUDIO'],
            demandOption: true,
        })
        .option('rate', {
            alias: 'r',
            describe: 'Audio sampling rate.',
            type: 'number',
            demandOption: true,
        })
        .option('num-channels', {
            alias: 'c',
            describe: 'Number of audio channels.',
            type: 'number',
            demandOption: true,
        })
        .option('max-alternatives', {
            describe: 'Number of speech recognition alternatives to return.',
            type: 'number',
            default: 1,
        })
        .option('perform-vad', {
            describe: 'Turn this off to disable voice activity detection. All audio is processed ' +
                'as though it were a single utterance.',
            type: 'boolean',
            default: true,
        })
        .option('silence-duration-threshold', {
            describe: 'Silence threshold in seconds for VAD to assume the current utterance is ended and ' +
                'the next utterance shall begin.',
            type: 'number',
            default: 0.6,
        })
        .option('language-code', {
            describe: 'Language for speech recognition.',
            type: 'string',
            choices: ['ru-RU'],
            default: 'ru-RU',
        })
        .option('automatic-punctuation', {
            describe: 'Turn this off to disable automatic punctuation in recognition results.',
            type: 'boolean',
            default: true,
        });
}

function streamingRecognitionOptions(yargs) {
    return baseRecognitionOptions(yargs)
        .option('interim-results', {
            describe: 'Yield interim results',
            type: 'boolean',
            default: false,
        })
        .option('single-utterance', {
            describe: 'Recognize only first utterance',
            type: 'boolean',
            default: false,
        });
}

function streamingSynthesisOptions(yargs) {
    return yargs
        .strict(true)
        .positional('input-text', {
            describe: 'Input text to synthesize.',
            type: 'string',
        })
        .positional('output-file', {
            describe: 'Output wav to save.',
            type: 'string',
        })
        .option('encoding', {
            alias: 'e',
            describe: 'Audio encoding',
            type: 'string',
            choices: ['LINEAR16', 'RAW_OPUS'],
            demandOption: true,
        })
        .option('rate', {
            alias: 'r',
            describe: 'Audio sample rate.',
            type: 'number',
            demandOption: true,
            choices: [8000, 16000, 24000, 48000],
        })
}

const buildRecognizeCommand = () => {
    return yargsLibrary
        .command('$0 <file>', 'recognize file', baseRecognitionOptions)
        .help();
};

const buildStreamingRecognizeCommand = () => {
    return yargsLibrary
        .command('$0 <file>', 'recognize stream file', streamingRecognitionOptions)
        .help();
};

const buildStreamingSynthesizeCommand = () => {
    return yargsLibrary
        .command('$0 <input-text> <output-file>', 'synthesize audio from text', streamingSynthesisOptions)
        .help();
};

module.exports = {
    buildRecognizeCommand,
    buildStreamingRecognizeCommand,
    buildStreamingSynthesizeCommand,
};
