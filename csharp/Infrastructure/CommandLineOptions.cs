using System.Collections.Generic;
using System.CommandLine;

namespace csharp.Infrastructure
{
    public static class CommandLineOptions
    {
        public static List<Option> CreateRecognitionOptions()
        {
            var sampleRateOption = new Option("--sample-rate");
            sampleRateOption.AddAlias("-r");
            var sampleRate = new Argument<uint>();
            sampleRateOption.Argument = sampleRate;

            var audioEncodingOption = new Option("--audio-encoding");
            audioEncodingOption.AddAlias("-e");
            var audioEncoding = new Argument<string>();
            audioEncodingOption.Argument = audioEncoding;

            var countAudioChannelOption = new Option("--channels-count");
            countAudioChannelOption.AddAlias("-c");
            var countAudioChannel = new Argument<uint>();
            countAudioChannelOption.Argument = countAudioChannel;

            var maxAlternativesOption = new Option("--max-alternatives");
            var maxAlternatives = new Argument<uint>(defaultValue: () => 1);
            maxAlternativesOption.Argument = maxAlternatives;

            var disableAutomaticPunctuationOption = new Option("--disable-automatic-punctuation");
            var disableAutomaticPunctation = new Argument<bool>(defaultValue: () => false);
            disableAutomaticPunctuationOption.Argument = disableAutomaticPunctation;

            var doNotPerformVadOption = new Option("--do-not-perform-vad");
            var doNotPerformVad = new Argument<bool>(defaultValue: () => false);
            doNotPerformVadOption.Argument = doNotPerformVad;

            var silenceDurationThresholdOption = new Option("--silence-duration-threshold");
            var silenceDurationThreshold = new Argument<float>(defaultValue: () => -1);
            silenceDurationThresholdOption.Argument = silenceDurationThreshold;

            var audioPathOption = new Option("--audio-path");
            audioPathOption.AddAlias("-p");
            var audioPath = new Argument<string>();
            audioPathOption.Argument = audioPath;

            var options = new List<Option>();
            options.Add(sampleRateOption);
            options.Add(countAudioChannelOption);
            options.Add(audioEncodingOption);
            options.Add(maxAlternativesOption);
            options.Add(disableAutomaticPunctuationOption);
            options.Add(doNotPerformVadOption);
            options.Add(silenceDurationThresholdOption);
            options.Add(audioPathOption);

            return options;
        }

        public static List<Option> CreateStreamingRecognitionOptions()
        {
            var enableInterimResultsOption = new Option("--enable-interim-results");
            var enableInterimResults = new Argument<bool>(defaultValue: () => false);
            enableInterimResultsOption.Argument = enableInterimResults;

            var singleUtteranceOption = new Option("--single-utterance");
            var singleUtterance = new Argument<bool>(defaultValue: () => false);
            singleUtteranceOption.Argument = singleUtterance;

            var options = CreateRecognitionOptions();
            options.Add(enableInterimResultsOption);
            options.Add(singleUtteranceOption);

            return options;
        }

        public static List<Option> CreateStreamingSynthesisOptions()
        {
            Option textOption = new Option("--synthesize-text");
            textOption.AddAlias("-t");
            var text = new Argument<string>();
            textOption.Argument = text;

            Option audioNameOption = new Option("--audio-name");
            audioNameOption.AddAlias("-o");
            var audioName = new Argument<string>();
            audioNameOption.Argument = audioName;

            var options = new List<Option>();
            options.Add(textOption);
            options.Add(audioNameOption);

            return options;
        }
    }
}