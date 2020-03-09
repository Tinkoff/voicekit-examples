using System;
using System.IO;
using System.CommandLine;
using System.CommandLine.Invocation;
using Tinkoff.Cloud.Stt.V1;
using Tinkoff.VoiceKit;
using NAudio.Wave;

namespace csharp.Infrastructure
{
    public static class CommandLineInterface
    {
        delegate void RecognitionHandler(
            uint sampleRate,
            string audioEncoding,
            uint channelsCount,
            uint maxAlternatives,
            bool disableAutomaticPunctation,
            bool doNotPerformVad,
            float silenceDurationThreshold,
            string audioPath
        );

        delegate void StreamingRecognitionHandler(
            uint sampleRate,
            string audioEncoding,
            uint channelsCount,
            uint maxAlternatives,
            bool disablePunctuation,
            bool disableAutomaticPunctation,
            float silenceDurationThreshold,
            string audioPath,
            bool enableInterimResults,
            bool singleUtterance
        );

        static VoiceKitClient _client;

        static CommandLineInterface()
        {
            string apiKey = Environment.GetEnvironmentVariable("VOICEKIT_API_KEY");
            string secretKey = Environment.GetEnvironmentVariable("VOICEKIT_SECRET_KEY");

            if (string.IsNullOrEmpty(apiKey))
                throw new ArgumentException("VOICEKIT_API_KEY does not exist in the enviroment variable");
            if (string.IsNullOrEmpty(secretKey))
                throw new ArgumentException("VOICEKIT_SECRET_KEY does not exist in the enviroment variable");

            _client = new VoiceKitClient(apiKey, secretKey);
        }

        public static Command CreateRecognitionCommand()
        {
            var recognitionCommand = new Command("recognize", "recognize audio");

            foreach (var option in CommandLineOptions.CreateRecognitionOptions())
                recognitionCommand.Add(option);

            RecognitionHandler handler = HandleRecognitionCommand;
            recognitionCommand.Handler = CommandHandler.Create(handler);

            return recognitionCommand;
        }

        public static Command CreateStreamingRecognitionCommand()
        {
            var streamingRecognitionCommand = new Command("streaming-recognize", "streaming recognize audio");

            foreach (var option in CommandLineOptions.CreateStreamingRecognitionOptions())
                streamingRecognitionCommand.Add(option);

            StreamingRecognitionHandler handler = HandleStreamingRecognitionCommand;
            streamingRecognitionCommand.Handler = CommandHandler.Create(handler);

            return streamingRecognitionCommand;
        }

        public static Command CreateStreamingSynthesisCommand()
        {
            var streamingSynthesisCommand = new Command("synthesize");

            foreach (var option in CommandLineOptions.CreateStreamingSynthesisOptions())
                streamingSynthesisCommand.AddOption(option);

            streamingSynthesisCommand.Handler = CommandHandler
            .Create<string, string>
            (
                (synthesizeText, audioNAme) =>
                {
                    _client.StreamingSynthesize(synthesizeText, audioNAme).Wait();
                }
            );

            return streamingSynthesisCommand;
        }

        public static void HandleRecognitionCommand(
            uint sampleRate,
            string audioEncoding,
            uint channelsCount,
            uint maxAlternatives,
            bool disableAutomaticPunctation,
            bool doNotPerformVad,
            float silenceDurationThreshold,
            string audioPath
        )
        {
            RecognitionConfig recognitionConfig = CreateRecognitionConfig(
                sampleRate,
                audioEncoding,
                channelsCount,
                maxAlternatives,
                disableAutomaticPunctation
            );

            ConfigurateVAD(
                recognitionConfig, 
                silenceDurationThreshold, 
                doNotPerformVad
            );

            using (var fileStream = GetAudioStream(audioPath, audioEncoding))
            {
                System.Console.WriteLine(_client.Recognize(recognitionConfig, fileStream));
            }
        }

        public static void HandleStreamingRecognitionCommand(
            uint sampleRate,
            string audioEncoding,
            uint channelsCount,
            uint maxAlternatives,
            bool disableAutomaticPunctation,
            bool doNotPerformVad,
            float silenceDurationThreshold,
            string audioPath,
            bool enableInterimResults,
            bool singleUtterance
        )
        {
            var streamingRecognitionConfig = CreateStreamingRecognitionConfig(
                sampleRate,
                audioEncoding,
                channelsCount,
                maxAlternatives,
                disableAutomaticPunctation,
                enableInterimResults,
                singleUtterance
            );
            

            ConfigurateVAD(
                streamingRecognitionConfig.Config, 
                silenceDurationThreshold, 
                doNotPerformVad
            );

            using (var stream = GetAudioStream(audioPath, audioEncoding))
            {
                _client.StreamingRecognize(streamingRecognitionConfig, stream).Wait();
            }
        }

        static RecognitionConfig CreateRecognitionConfig(
            uint sampleRate,
            string encoding,
            uint countChannel,
            uint maxAlternatives,
            bool disablePunctuation
        )
        {
            var recognitionConfig = new RecognitionConfig();
            recognitionConfig.SampleRateHertz = sampleRate;
            recognitionConfig.Encoding = GetAudioEncodingSTT(encoding);
            recognitionConfig.NumChannels = countChannel;
            recognitionConfig.EnableAutomaticPunctuation = !disablePunctuation;
            recognitionConfig.MaxAlternatives = maxAlternatives;

            return recognitionConfig;
        }

        static StreamingRecognitionConfig CreateStreamingRecognitionConfig(
            uint sampleRate,
            string audioEncoding,
            uint countChannel,
            uint maxAlternatives,
            bool disableAutomaticPunctuation,
            bool enableInterimResults,
            bool singleUtterance
        )
        {
            var streamingRecognitionConfig = new StreamingRecognitionConfig();

            streamingRecognitionConfig.Config = CreateRecognitionConfig(
                sampleRate,
                audioEncoding,
                countChannel,
                maxAlternatives,
                disableAutomaticPunctuation
            );

            streamingRecognitionConfig.InterimResultsConfig = new InterimResultsConfig
            {
                EnableInterimResults = enableInterimResults,
            };

            streamingRecognitionConfig.SingleUtterance = singleUtterance;

            return streamingRecognitionConfig;
        }

        static void ConfigurateVAD(
            RecognitionConfig config,
            float silenceDurationThreshold,
            bool doNotPerformVAD
        )
        {
            if (silenceDurationThreshold >= 0)
                config.VadConfig = new VoiceActivityDetectionConfig
                {
                    SilenceDurationThreshold = silenceDurationThreshold
                };
            else
                config.DoNotPerformVad = doNotPerformVAD;
        }

        static AudioEncoding GetAudioEncodingSTT(string encoding)
        {
            switch (encoding)
            {
                case "MPEG_AUDIO":
                    return AudioEncoding.MpegAudio;
                case "LINEAR16":
                case "WAV":
                    return AudioEncoding.Linear16;
                case "MULAW":
                    return AudioEncoding.Mulaw;
                case "ALAW":
                    return AudioEncoding.Alaw;
                case "RAW_OPUS":
                    return AudioEncoding.RawOpus;
                default:
                    throw new ArgumentException($"{encoding} is unsupported audio format");
            }
        }

        public static Stream GetAudioStream(string path, string audioEncoding)
        {
            if (audioEncoding == "WAV")
                return new WaveFileReader(path);

            return new FileStream(path, FileMode.Open);
        }
    }
}