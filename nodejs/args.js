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
            choices: ['LINEAR16', 'LINEAR32F', 'ALAW', 'MULAW', 'MPEG_AUDIO'],
            demandOption: true,
        })
        .option('rate', {
            alias: 'r',
            describe: 'Sampling rate of the audio.',
            type: 'number',
            demandOption: true,
        })
        .option('num-channels', {
            alias: 'c',
            describe: 'Number of channels in the audio.',
            type: 'number',
            demandOption: true,
        })
        .option('max-alternatives', {
            descibe: 'Number of maximum speech recognition alternatives to return',
            type: 'number',
            default: 1,
        })
        .option('perform-vad', {
            type: 'boolean',
            default: true,
        })
        .option('silence-duration-threshold', {
            type: 'number',
            default: 0.6,
        })
        .option('language-code', {
            type: 'string',
            choices: ['ru-RU'],
            default: 'ru-RU',
        })
        .option('automatic-punctuation', {
            type: 'boolean',
            default: true,
        });
}

function streamingRecognitionOptions(yargs) {
    return baseRecognitionOptions(yargs)
        .option('interim-results', {
            type: 'boolean',
            default: false,
        })
        .option('single-utterance', {
            type: 'boolean',
            default: false,
        });
}

function streamingSynthesisOptions(yargs) {
    return yargs
        .strict(true)
        .positional('input-text', {
            describe: 'text to synthesize',
            type: 'string',
        })
        .positional('output-file', {
            describe: 'output file',
            type: 'string',
        })
        .option('encoding', {
            alias: 'e',
            describe: 'Audio encoding.',
            type: 'string',
            choices: ['LINEAR16', 'RAW_OPUS'],
            demandOption: true,
        })
        .option('rate', {
            alias: 'r',
            describe: 'Sampling rate of the audio.',
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
