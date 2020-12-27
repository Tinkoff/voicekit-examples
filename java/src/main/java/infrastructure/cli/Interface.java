package infrastructure.cli;

import voicekit.utils.Printer;
import voicekit.Client;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import tinkoff.cloud.stt.v1.Stt;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Interface {
    private static final Logger logger =
            Logger.getLogger(Interface.class.getName());

    static final String RECOGNIZE_COMMAND = "recognize";
    static final String STREAMING_RECOGNIZE_COMMAND = "streaming-recognize";
    static final String SYNTHESIS_COMMAND = "synthesize";
    static final String RECOGNIZE_THROUGH_MICROPHONE_COMMAND = "microphone";

    Client client;

    public Interface() {
        String apiKey = System.getenv().get("VOICEKIT_API_KEY");
        String secretKey = System.getenv().get("VOICEKIT_SECRET_KEY");

        client = new Client(apiKey, secretKey);
    }

    public void start(String[] args) {
        String executableCommand = args[0];
        String[] params = getParams(args);
        switch (executableCommand) {
            case RECOGNIZE_COMMAND:
                executeRecognize(params);
                break;
            case STREAMING_RECOGNIZE_COMMAND:
                executeStreamingRecognize(params);
                break;
            case SYNTHESIS_COMMAND:
                executeSynthesis(params);
                break;
            case RECOGNIZE_THROUGH_MICROPHONE_COMMAND:
                executeMicrophone(params);
                break;
            default:
                throw new IllegalArgumentException("Command is not available.");
        }
    }

    private String[] getParams(String[] args) {
        String[] params = new String[args.length - 1];
        System.arraycopy(args, 1, params, 0, params.length);
        return params;
    }

    void executeRecognize(String[] args) {
        try {
            CommandLine commandLine = RequestBuilder.parseRecognizeRequest(args);
            Stt.RecognitionConfig config = RequestBuilder.buildRecognizeRequestConfig(commandLine);
            try(InputStream stream = RequestBuilder.getAudioStream(commandLine)) {
                client.recognize(config, stream);
            }
        } catch (IOException | ParseException e) {
            logger.log(Level.SEVERE, "Error in recognize", e);
        }
    }

    void executeStreamingRecognize(String[] args) {
        try {
            CommandLine commandLine = RequestBuilder.parseStreamingRecognizeRequest(args);
            Stt.StreamingRecognitionConfig config = RequestBuilder.buildStreamingRecognizeRequestConfig(commandLine);
            try(InputStream stream = RequestBuilder.getAudioStream(commandLine)) {
                client.streamingRecognize(config, stream);
            }
        } catch (IOException | ParseException e) {
            logger.log(Level.SEVERE, "Error in streaming recognize", e);
        }
    }

    void executeSynthesis(String[] args) {
        try {
            CommandLine commandLine = RequestBuilder.parseSynthesisRequest(args);
            String text = commandLine.getOptionValue(Params.TEXT);
            String outputPath = commandLine.getOptionValue(Params.OUTPUT);
            String voice;
            if (commandLine.hasOption(Params.VOICE)) {
                voice = commandLine.getOptionValue(Params.VOICE);
            } else {
                voice = "maxim";
            }

            client.streamingSynthesis(text, outputPath, voice);
        } catch (IOException | ParseException e) {
            logger.log(Level.SEVERE, "Error in streaming synthesis", e);
        }
    }

    void executeMicrophone(String[] args) {
        try {
            CommandLine commandLine = RequestBuilder.parseMicrophoneRequest(args);

            Stt.StreamingRecognitionConfig config = RequestBuilder.buildMicrophoneRecognizeConfig(commandLine);
            client.recognizeThroughMicrophone(config);
        } catch (LineUnavailableException | ParseException | IOException e) {
            logger.log(Level.SEVERE, "Error in microphone recognition", e);
        }
    }
}
