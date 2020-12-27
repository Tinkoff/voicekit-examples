package voicekit;

import voicekit.response.SttRecognizeHandler;
import voicekit.response.SttStreamingRecognizeHandler;
import voicekit.response.TtsStreamingSynthesisHandler;
import voicekit.utils.AudioParser;
import voicekit.utils.Printer;
import com.google.protobuf.ByteString;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import tinkoff.cloud.stt.v1.SpeechToTextGrpc;
import tinkoff.cloud.stt.v1.Stt;
import tinkoff.cloud.tts.v1.TextToSpeechGrpc;
import tinkoff.cloud.tts.v1.Tts;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.IOException;
import java.io.InputStream;

public class Client {
    private static final int CHUNK_SIZE = 8192;
    private static final int SYNTHESIS_SAMPLE_RATE = 48000;

    SpeechToTextGrpc.SpeechToTextStub clientSTT;
    TextToSpeechGrpc.TextToSpeechStub clientTTS;

    Auth sttAuth;
    Auth ttsAuth;

    public Client(String apiKey, String secretKey) {
        sttAuth = new Auth(apiKey, secretKey, "tinkoff.cloud.stt");
        ttsAuth = new Auth(apiKey, secretKey, "tinkoff.cloud.tts");

        Channel sttChannel = ManagedChannelBuilder.forTarget("stt.tinkoff.ru:443").build();
        Channel ttsChannel = ManagedChannelBuilder.forTarget("tts.tinkoff.ru:443").build();

        clientSTT = SpeechToTextGrpc.newStub(sttChannel).withCallCredentials(sttAuth);
        clientTTS = TextToSpeechGrpc.newStub(ttsChannel).withCallCredentials(ttsAuth);
    }

    public void recognize(Stt.RecognitionConfig config, InputStream stream) throws IOException {
        ByteString content = ByteString.copyFrom(stream.readAllBytes());
        Stt.RecognitionAudio audio = Stt.RecognitionAudio.newBuilder().setContent(content).build();
        Stt.RecognizeRequest request = Stt.RecognizeRequest.newBuilder()
                .setConfig(config)
                .setAudio(audio)
                .build();
        SttRecognizeHandler responseHandler = new SttRecognizeHandler();

        clientSTT.recognize(request, responseHandler);
        responseHandler.waitOnComplete();
    }

    public void streamingRecognize(Stt.StreamingRecognitionConfig config, InputStream stream) {
        SttStreamingRecognizeHandler responseHandler = new SttStreamingRecognizeHandler();
        Stt.StreamingRecognizeRequest.Builder builder = Stt.StreamingRecognizeRequest.newBuilder();

        StreamObserver<Stt.StreamingRecognizeRequest> requestsHandler = clientSTT.streamingRecognize(responseHandler);

        Stt.StreamingRecognizeRequest requestConfig = builder.setStreamingConfig(config).build();
        requestsHandler.onNext(requestConfig);

        byte[] audioBuffer = new byte[CHUNK_SIZE];
        try {
            while (stream.read(audioBuffer) > 0) {
                ByteString content = ByteString.copyFrom(audioBuffer);
                Stt.StreamingRecognizeRequest request = builder.setAudioContent(content).build();
                requestsHandler.onNext(request);
            }
        } catch (IOException e) {
            requestsHandler.onError(e);
            return;
        }
        requestsHandler.onCompleted();
        responseHandler.waitOnComplete();
    }

    public void recognizeThroughMicrophone(Stt.StreamingRecognitionConfig config) throws LineUnavailableException, IOException {
        TargetDataLine linear = AudioParser.getMicrophoneStream();
        if (linear == null) {
            Printer.getPrinter().println("Line not supported");
            return;
        }

        try(InputStream stream = new AudioInputStream(linear)) {
            linear.start();

            new Thread(() -> this.streamingRecognize(config, stream)).start();
            Printer.getPrinter().println("...record! Click enter for exit.");
            waitOnInput();
        } finally {
            linear.stop();
            linear.close();
        }
    }

    public void streamingSynthesis(String inputText, String outputAudioPath, String voice) throws IOException {
        Tts.AudioConfig config = Tts.AudioConfig.newBuilder()
                .setAudioEncoding(Tts.AudioEncoding.LINEAR16)
                .setSampleRateHertz(SYNTHESIS_SAMPLE_RATE).build();
        Tts.SynthesizeSpeechRequest request = Tts.SynthesizeSpeechRequest.newBuilder()
                .setAudioConfig(config)
                .setVoice(Tts.VoiceSelectionParams.newBuilder().setName(voice).build())
                .setInput(Tts.SynthesisInput.newBuilder().setText(inputText)).build();
        TtsStreamingSynthesisHandler responseHandler = new TtsStreamingSynthesisHandler();

        clientTTS.streamingSynthesize(request, responseHandler);
        responseHandler.waitOnComplete();

        AudioParser.saveAudioInWAV(outputAudioPath, responseHandler.getAudioContent(), SYNTHESIS_SAMPLE_RATE);
    }

    void waitOnInput() {
        System.out.println();
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
