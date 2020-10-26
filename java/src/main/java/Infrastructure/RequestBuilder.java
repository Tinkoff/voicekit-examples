package Infrastructure;

import org.apache.commons.cli.*;
import tinkoff.cloud.stt.v1.Stt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class RequestBuilder {
    static CommandLineParser parser = new DefaultParser();

    public static Stt.RecognitionConfig buildRecognizeRequestConfig(CommandLine commandLine) {
        Stt.RecognitionConfig.Builder recognitionConfigBuilder = Stt.RecognitionConfig.newBuilder();

        String encoding = commandLine.getOptionValue(CommandLineParams.encoding);
        recognitionConfigBuilder.setEncoding(parseAudioEncoding(encoding));

        int sampleRate = Integer.parseInt(commandLine.getOptionValue(CommandLineParams.sampleRate));
        recognitionConfigBuilder.setSampleRateHertz(sampleRate);

        int channelsCount = Integer.parseInt(commandLine.getOptionValue(CommandLineParams.channelsCount));
        recognitionConfigBuilder.setNumChannels(channelsCount);

        addAdditionalRecognizeOptions(recognitionConfigBuilder, commandLine);

        return recognitionConfigBuilder.build();
    }

    public static Stt.StreamingRecognitionConfig buildStreamingRecognizeRequestConfig(CommandLine commandLine) {
        Stt.StreamingRecognitionConfig.Builder builder = Stt.StreamingRecognitionConfig.newBuilder();
        Stt.RecognitionConfig recognitionConfig = buildRecognizeRequestConfig(commandLine);
        builder.setConfig(recognitionConfig);
        addAdditionalStreamingRecognizeOptions(builder, commandLine);
        return builder.build();
    }

    public static Stt.StreamingRecognitionConfig buildMicrophoneRecognizeConfig(CommandLine commandLine) {
        Stt.RecognitionConfig.Builder configBuilder = Stt.RecognitionConfig
                .newBuilder()
                .setSampleRateHertz(16000)
                .setNumChannels(1)
                .setEncoding(Stt.AudioEncoding.LINEAR16);

        addAdditionalRecognizeOptions(configBuilder, commandLine);

        Stt.StreamingRecognitionConfig.Builder streamingConfigBuilder = Stt.StreamingRecognitionConfig
                .newBuilder()
                .setConfig(configBuilder.build());
        addAdditionalStreamingRecognizeOptions(streamingConfigBuilder, commandLine);
        return streamingConfigBuilder.build();
    }

    public static CommandLine parseRecognizeRequest(String[] params) throws ParseException {
        Options options = CommandLineOptions.createRecognitionOptions();
        return parser.parse(options, params);
    }

    public static CommandLine parseStreamingRecognizeRequest(String[] params) throws ParseException {
        Options options = CommandLineOptions.createStreamingRecognitionOptions();
        return parser.parse(options, params);
    }

    public static CommandLine parseMicrophoneRequest(String[] params) throws ParseException {
        Options options = CommandLineOptions.createMicrophoneOptions();
        return parser.parse(options, params);
    }

    public static CommandLine parseSynthesisRequest(String[] params) throws ParseException {
        Options options = CommandLineOptions.createSynthesisOption();
        return parser.parse(options, params);
    }

    public static InputStream getAudioStream(CommandLine commandLine) throws FileNotFoundException {
        String audioPath = commandLine.getOptionValue(CommandLineParams.audioPath);
        return new FileInputStream(audioPath);
    }

    static Stt.AudioEncoding parseAudioEncoding(String value) {
        switch (value) {
            case "MPEG_AUDIO":
                return Stt.AudioEncoding.MPEG_AUDIO;
            case "LINEAR_16":
            case "WAV":
                return Stt.AudioEncoding.LINEAR16;
            case "ALAW":
                return Stt.AudioEncoding.ALAW;
            case "RAW_OPUS":
                return Stt.AudioEncoding.RAW_OPUS;
            default:
                // TODO: may be throw exception
                return Stt.AudioEncoding.ENCODING_UNSPECIFIED;
        }
    }

    static void addAdditionalRecognizeOptions(Stt.RecognitionConfig.Builder builder, CommandLine commandLine) {
        if (commandLine.hasOption(CommandLineParams.maxAlternatives)) {
            int maxAlternatives = Integer.parseInt(commandLine.getOptionValue(CommandLineParams.maxAlternatives));
            builder.setMaxAlternatives(maxAlternatives);
        }

        if (commandLine.hasOption(CommandLineParams.doNotPerformedVAD)) {
            boolean doNotPerformedVAD = Boolean.parseBoolean(commandLine.getOptionValue(CommandLineParams.doNotPerformedVAD));
            builder.setDoNotPerformVad(doNotPerformedVAD);
        }

        if (commandLine.hasOption(CommandLineParams.disablePunctuation)) {
            boolean disablePunctuation = Boolean.parseBoolean(commandLine.getOptionValue(CommandLineParams.disablePunctuation));
            builder.setEnableAutomaticPunctuation(!disablePunctuation);
        } else {
            builder.setEnableAutomaticPunctuation(true);
        }

        if (commandLine.hasOption(CommandLineParams.silenceDurationThreshold)) {
            float silenceDurationThreshold = Float.parseFloat(commandLine.getOptionValue(CommandLineParams.silenceDurationThreshold));
            Stt.VoiceActivityDetectionConfig VADConfig = Stt.VoiceActivityDetectionConfig
                    .newBuilder()
                    .setSilenceDurationThreshold(silenceDurationThreshold)
                    .build();
            builder.setVadConfig(VADConfig);
        }
    }

    static void addAdditionalStreamingRecognizeOptions(Stt.StreamingRecognitionConfig.Builder builder, CommandLine commandLine) {
        if (commandLine.hasOption(CommandLineParams.singleUtterance)) {
            boolean singleUtterance = Boolean.parseBoolean(commandLine.getOptionValue(CommandLineParams.singleUtterance));
            builder.setSingleUtterance(singleUtterance);
        }

        if (commandLine.hasOption(CommandLineParams.enableInterimResults)) {
            boolean enableInterimResults = Boolean.parseBoolean(commandLine.getOptionValue(CommandLineParams.enableInterimResults));
            builder.setInterimResultsConfig(
                    Stt.InterimResultsConfig
                            .newBuilder()
                            .setEnableInterimResults(enableInterimResults)
                            .build());
        }
    }
}

class CommandLineOptions {
    static Options createRecognitionOptions() {
        Option audioPath = new Option("p", true, "");
        audioPath.setLongOpt(CommandLineParams.audioPath);
        audioPath.setRequired(true);

        Option sampleRate = new Option("r", true, "");
        sampleRate.setLongOpt(CommandLineParams.sampleRate);
        sampleRate.setRequired(true);

        Option channelCount = new Option("c", true, "");
        channelCount.setLongOpt(CommandLineParams.channelsCount);
        channelCount.setRequired(true);

        Option audioEncoding = new Option("e", true, "");
        audioEncoding.setLongOpt(CommandLineParams.encoding);
        audioEncoding.setRequired(true);

        Options additionalOptions = createAdditionalRecognizeOptions();

        Options options = new Options();
        options.addOption(audioPath);
        options.addOption(sampleRate);
        options.addOption(channelCount);
        options.addOption(audioEncoding);

        for (Option option: additionalOptions.getOptions())
            options.addOption(option);

        return options;
    }

    static Options createAdditionalRecognizeOptions() {
        Option maxAlternatives = new Option("ma", true, "");
        maxAlternatives.setLongOpt(CommandLineParams.maxAlternatives);

        Option doNotPerformVAD = new Option("pv", true, "");
        doNotPerformVAD.setLongOpt(CommandLineParams.doNotPerformedVAD);

        Option silenceDuration = new Option("sd", true, "");
        silenceDuration.setLongOpt(CommandLineParams.silenceDurationThreshold);

        Option enablePunctuation = new Option("pc", true, "");
        enablePunctuation.setLongOpt(CommandLineParams.disablePunctuation);

        Options options = new Options();
        options.addOption(maxAlternatives);
        options.addOption(doNotPerformVAD);
        options.addOption(silenceDuration);
        options.addOption(enablePunctuation);

        return options;
    }

    static Options createStreamingRecognitionOptions() {
        Options options = createRecognitionOptions();
        Options additionalOptions = createAdditionalStreamingRecognitionOptions();

        for (Option option: additionalOptions.getOptions())
            options.addOption(option);

        return options;
    }

    static Options createAdditionalStreamingRecognitionOptions() {
        Options options = new Options();

        Option enableInterimResults = new Option("eir", true, "");
        enableInterimResults.setLongOpt(CommandLineParams.enableInterimResults);

        Option singleUtterances = new Option("su", true, "");
        singleUtterances.setLongOpt(CommandLineParams.singleUtterance);

        options.addOption(enableInterimResults);
        options.addOption(singleUtterances);

        return options;
    }

    static Options createMicrophoneOptions() {
        Options options = createAdditionalRecognizeOptions();
        Options streamingOptions = createAdditionalStreamingRecognitionOptions();

        for (Option option: streamingOptions.getOptions())
            options.addOption(option);

        return options;
    }

    static Options createSynthesisOption() {
        Options options = new Options();

        Option text = new Option("t", true, "");
        text.setLongOpt("text");
        text.setRequired(true);

        Option output = new Option("o", true, "");
        output.setLongOpt("output");
        output.setRequired(true);

        Option voice = new Option("v", true, "");
        voice.setLongOpt("voice");

        options.addOption(text);
        options.addOption(output);
        options.addOption(voice);

        return options;
    }
}
