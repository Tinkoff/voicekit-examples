package Infrastructure;

import VoiceKit.Client;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import tinkoff.cloud.stt.v1.Stt;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.io.InputStream;

public class CommandLineInterface {
    static final String RecognizeCommand = "recognize";
    static final String StreamingRecognizeCommand = "streaming-recognize";
    static final String SynthesisCommand = "synthesize";
    static final String RecognizeThrowMicrophoneCommand = "microphone";

    Client _client;

    public CommandLineInterface() {
        String apiKey = System.getenv().get("VOICEKIT_API_KEY");
        String secretKey = System.getenv().get("VOICEKIT_SECRET_KEY");

        _client = new Client(apiKey, secretKey);
    }

    public void start(String[] args) {
        String executableCommand = args[0];
        String[] params = getParams(args);
        switch (executableCommand) {
            case RecognizeCommand:
                executeRecognize(params);
                break;
            case StreamingRecognizeCommand:
                executeStreamingRecognize(params);
                break;
            case SynthesisCommand:
                executeSynthesis(params);
                break;
            case RecognizeThrowMicrophoneCommand:
                executeMicrophone(params);
                break;
            default:
                throw new IllegalArgumentException("Command is not available.");
        }
    }

    private String[] getParams(String[] args) {
        String[] params = new String[args.length - 1];
        if (params.length >= 0)
            System.arraycopy(args, 1, params, 0, params.length);
        return params;
    }

    void executeRecognize(String[] args) {
        try {
            CommandLine commandLine = RequestBuilder.parseRecognizeRequest(args);
            Stt.RecognitionConfig config = RequestBuilder.buildRecognizeRequestConfig(commandLine);
            InputStream stream = RequestBuilder.getAudioStream(commandLine);

            System.out.println(_client.Recognize(config, stream));
        } catch (IOException | InterruptedException | ParseException e) {
            e.printStackTrace();
        }
    }

    void executeStreamingRecognize(String[] args) {
        try {
            CommandLine commandLine = RequestBuilder.parseStreamingRecognizeRequest(args);
            Stt.StreamingRecognitionConfig config = RequestBuilder.buildStreamingRecognizeRequestConfig(commandLine);
            InputStream stream = RequestBuilder.getAudioStream(commandLine);

            _client.StreamingRecognize(config, stream);
        } catch (IOException | InterruptedException | ParseException e) {
            e.printStackTrace();
        }
    }

    void executeSynthesis(String[] args) {
        try {
            CommandLine commandLine = RequestBuilder.parseSynthesisRequest(args);
            String text = commandLine.getOptionValue(CommandLineParams.text);
            String outputPath = commandLine.getOptionValue(CommandLineParams.output);
            String voice;
            if (commandLine.hasOption(CommandLineParams.voice)) {
                voice = commandLine.getOptionValue(CommandLineParams.voice);
            } else {
                voice = "maxim";
            }

            _client.StreamingSynthesis(text, outputPath, voice);
        } catch (IOException | InterruptedException | ParseException e) {
            e.printStackTrace();
        }
    }

    void executeMicrophone(String[] args) {
        try {
            CommandLine commandLine = RequestBuilder.parseMicrophoneRequest(args);

            Stt.StreamingRecognitionConfig config = RequestBuilder.buildMicrophoneRecognizeConfig(commandLine);
            _client.RecognizeThrowMicrophone(config);
        } catch (LineUnavailableException | ParseException e) {
            e.printStackTrace();
        }
    }
}
