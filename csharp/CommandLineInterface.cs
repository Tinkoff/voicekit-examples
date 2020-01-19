using System;
using System.Collections.Generic;
using System.CommandLine;
using System.CommandLine.Invocation;
using Tinkoff.Cloud.Stt.V1;

namespace Tinkoff.VoiceKit
{
    public static class CommandLineInterface
    {
        static VoiceKitClient _client;
        static CommandLineInterface()
        {
            string apiKey = Environment.GetEnvironmentVariable("VOICEKIT_API_KEY");
            string secretKey = Environment.GetEnvironmentVariable("VOICEKIT_SECRET_KEY");

            if (string.IsNullOrEmpty(apiKey))
                throw new ArgumentException("VOICEKIT_API_KEY does not exist in enviroment variable");
            if (string.IsNullOrEmpty(secretKey))
                throw new ArgumentException("VOICEKIT_SECRET_KEY does not exist in enviroment variable");

            _client = new VoiceKitClient(apiKey, secretKey);
        }
        public static Command CreateRecognizeCommand()
        {
            var commandRecognize = new Command("recognize", "recognize audio");

            foreach (var option in CreateRecognizeOptions())
                commandRecognize.Add(option);

            commandRecognize.Handler = CommandHandler.
            Create<uint, string, uint, uint, bool, string>
            (
                (sampleRate,
                audioEncoding,
                countChannel,
                maxAlternatives,
                disablePunctuation,
                audioPath) =>
                {
                    RecognitionConfig recognizeConfig = CreateRecognizeConfig(
                        sampleRate,
                        audioEncoding,
                        countChannel,
                        maxAlternatives,
                        disablePunctuation);
                    System.Console.WriteLine(_client.Rcognize(recognizeConfig, audioPath));
                });

            return commandRecognize;
        }

        public static Command CreateStreamingRecognizeCommand()
        {
            var commandStreamingRecognize = new Command("streaming-recognize", "streaming recognize audio");

            foreach (var option in CreateStreamingRecognitionOptions())
                commandStreamingRecognize.Add(option);

            commandStreamingRecognize.Handler = CommandHandler.
            Create<uint, string, uint, uint, bool, string, bool>
            (
                (sampleRate,
                audioEncoding,
                countChannel,
                maxAlternatives,
                disableAutomaticPunctuation,
                audioPath,
                enableInterimResults) =>
                {
                    var streamingRecognizeConfig = CreateStreamingRecognizeConfig(
                        sampleRate,
                        audioEncoding,
                        countChannel,
                        maxAlternatives,
                        disableAutomaticPunctuation,
                        enableInterimResults);
                    _client.StreamingRecognize(streamingRecognizeConfig, audioPath).Wait();
                });

            return commandStreamingRecognize;
        }
        
        public static Command CreateStreamingSynthesizeCommand()
        {
            var commandStreamingSynthesize = new Command("synthesize", "streaming synthesize command");

            Option audioTextOption = new Option("--synthesize-text", "text, that you wont synthesize");
            audioTextOption.AddAlias("-t");
            var audioText = new Argument<string>();
            audioTextOption.Argument = audioText;

            Option audioNameOption = new Option("--audio-name", "name of audio that will be save");
            audioNameOption.AddAlias("-o");
            var audioName = new Argument<string>(defaultValue: () => "./audio.wav");
            audioNameOption.Argument = audioName;

            commandStreamingSynthesize.AddOption(audioTextOption);
            commandStreamingSynthesize.AddOption(audioNameOption);

            commandStreamingSynthesize.Handler = CommandHandler.
            Create<string, string>
            (
                (synthesizeText, audioNAme) =>
                {
                    _client.StreamingSynthesize(synthesizeText, audioNAme).Wait();
                }
            );
            
            return commandStreamingSynthesize;
        }

        static List<Option> CreateRecognizeOptions()
        {
            List<Option> recognizeOptions = new List<Option>();

            var sampleRateOption = new Option("--sample-rate");
            sampleRateOption.AddAlias("-r");
            var sampleRate = new Argument<uint>();
            sampleRateOption.Argument = sampleRate;

            var audioEncodingOption = new Option("--audio-encoding");
            audioEncodingOption.AddAlias("-e");
            var audioEncoding = new Argument<string>();
            audioEncodingOption.Argument = audioEncoding;

            var countAudioChannelOption = new Option("--count-channel");
            countAudioChannelOption.AddAlias("-c");
            var countAudioChannel = new Argument<uint>();
            countAudioChannelOption.Argument = countAudioChannel;

            var maxAlternativesOption = new Option("--max-alternatives");
            var maxAlternatives = new Argument<uint>(defaultValue: () => 1);
            maxAlternativesOption.Argument = maxAlternatives;

            var disableAutomaticPunctuationOption = new Option("--disable-automatic-punctuation");
            var disableAutomaticPunctation = new Argument<bool>(defaultValue: () => false);
            disableAutomaticPunctuationOption.Argument = disableAutomaticPunctation;

            var audioPathOption = new Option("--audio-path");
            audioPathOption.AddAlias("-p");
            var audioPath = new Argument<string>();
            audioPathOption.Argument = audioPath;

            recognizeOptions.Add(sampleRateOption);
            recognizeOptions.Add(countAudioChannelOption);
            recognizeOptions.Add(audioEncodingOption);
            recognizeOptions.Add(maxAlternativesOption);
            recognizeOptions.Add(disableAutomaticPunctuationOption);
            recognizeOptions.Add(audioPathOption);

            return recognizeOptions;
        }

        static List<Option> CreateStreamingRecognitionOptions()
        {
            List<Option> options  = CreateRecognizeOptions();

            var enableInterimResultsOption = new Option("--enable-interim-results");
            var enableInterimResults = new Argument<bool>(defaultValue: () => false);
            enableInterimResultsOption.Argument = enableInterimResults;

            options.Add(enableInterimResultsOption);

            return options;
        }

        static AudioEncoding GetAudioEncodingSTT(string encoding)
        {
            switch (encoding)
            {
                case "MPEG_AUDIO":
                    return AudioEncoding.MpegAudio;
                case "LINEAR16":
                    return AudioEncoding.Linear16;
                case "MULAW":
                    return AudioEncoding.Mulaw;
                case "ALAW":
                    return AudioEncoding.Alaw;
                case "RAW_OPUS":
                    return AudioEncoding.RawOpus;
            }
            return AudioEncoding.EncodingUnspecified;
        }

        static RecognitionConfig CreateRecognizeConfig(
            uint sampleRate,
            string encoding,
            uint countChannel,
            uint maxAlternatives,
            bool disablePunctuation
        )
        {
            var recognizeCongig = new RecognitionConfig();
            recognizeCongig.SampleRateHertz = sampleRate;
            recognizeCongig.Encoding = GetAudioEncodingSTT(encoding);
            recognizeCongig.NumChannels = countChannel;
            recognizeCongig.EnableAutomaticPunctuation = !disablePunctuation;
            recognizeCongig.MaxAlternatives = maxAlternatives;
            return recognizeCongig;
        }

        static StreamingRecognitionConfig CreateStreamingRecognizeConfig(
            uint sampleRate,
            string audioEncoding,
            uint countChannel,
            uint maxAlternatives,
            bool disableAutomaticPunctuation,
            bool enableInterimResults
        )
        {
            var streamingRecognizeConfig = new StreamingRecognitionConfig();

            streamingRecognizeConfig.Config = CreateRecognizeConfig
            (
                sampleRate,
                audioEncoding,
                countChannel,
                maxAlternatives,
                disableAutomaticPunctuation
            );

            streamingRecognizeConfig.InterimResultsConfig = new InterimResultsConfig()
            {
                EnableInterimResults = enableInterimResults
            };

            return streamingRecognizeConfig;
        }
    }
}