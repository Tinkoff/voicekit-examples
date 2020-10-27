package VoiceKit;

import VoiceKit.Utils.Printer;
import VoiceKit.ResponseHandlers.SttRecognizeHandler;
import VoiceKit.ResponseHandlers.SttStreamingRecognizeHandler;
import VoiceKit.ResponseHandlers.TtsStreamingSynthesisHandler;
import VoiceKit.Utils.AudioParser;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Client {
    private static final int CHUNK_SIZE = 8192;
    private static final int SYNTHESIS_SAMPLE_RATE = 48000;

    SpeechToTextGrpc.SpeechToTextStub _clientSTT;
    TextToSpeechGrpc.TextToSpeechStub _clientTTS;

    Auth _sttAuth;
    Auth _ttsAuth;

    public Client(String apiKey, String secretKey) {
        _sttAuth = new Auth(apiKey, secretKey, "tinkoff.cloud.stt");
        _ttsAuth = new Auth(apiKey, secretKey, "tinkoff.cloud.tts");

        Channel sttChannel = ManagedChannelBuilder.forTarget("stt.tinkoff.ru:443").build();
        Channel ttsChannel = ManagedChannelBuilder.forTarget("tts.tinkoff.ru:443").build();

        _clientSTT = SpeechToTextGrpc.newStub(sttChannel).withCallCredentials(_sttAuth);
        _clientTTS = TextToSpeechGrpc.newStub(ttsChannel).withCallCredentials(_ttsAuth);
    }

    public String Recognize(Stt.RecognitionConfig config, InputStream stream) throws IOException, InterruptedException {
        ByteString content = ByteString.copyFrom(stream.readAllBytes());
        Stt.RecognitionAudio audio = Stt.RecognitionAudio.newBuilder().setContent(content).build();
        Stt.RecognizeRequest request = Stt.RecognizeRequest.newBuilder()
                .setConfig(config)
                .setAudio(audio)
                .build();
        SttRecognizeHandler responseHandler = new SttRecognizeHandler();

        _clientSTT.recognize(request, responseHandler);
        responseHandler.waitOnComplete();

        return responseHandler.getText();
    }

    public void StreamingRecognize(Stt.StreamingRecognitionConfig config, InputStream stream) throws InterruptedException {
        SttStreamingRecognizeHandler responseHandler = new SttStreamingRecognizeHandler();
        Stt.StreamingRecognizeRequest.Builder builder = Stt.StreamingRecognizeRequest.newBuilder();

        StreamObserver<Stt.StreamingRecognizeRequest> requestsHandler = _clientSTT.streamingRecognize(responseHandler);

        Stt.StreamingRecognizeRequest requestConfig = builder.setStreamingConfig(config).build();
        requestsHandler.onNext(requestConfig);

        byte[] audioBuffer = new byte[CHUNK_SIZE];
        try {
            while(stream.read(audioBuffer) > 0) {
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

    public void RecognizeThrowMicrophone(Stt.StreamingRecognitionConfig config) throws LineUnavailableException {
        TargetDataLine linear = AudioParser.getMicrophoneStream();
        if (linear == null) {
            Printer.getPrinter().println("Line not supported");
            return;
        }

        try {
            linear.start();
            InputStream stream =new AudioInputStream(linear);

            new Thread(() -> {
                try {
                    this.StreamingRecognize(config, stream);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            Printer.getPrinter().println("...record! Click enter for exit.");
            waitOnInput();
        }  finally {
            linear.stop();
            linear.close();
        }
    }

    public void StreamingSynthesis(String inputText, String outputAudioPath, String voice) throws IOException, InterruptedException {
        Tts.AudioConfig config = Tts.AudioConfig.newBuilder()
                .setAudioEncoding(Tts.AudioEncoding.LINEAR16)
                .setSampleRateHertz(SYNTHESIS_SAMPLE_RATE).build();
        Tts.SynthesizeSpeechRequest request = Tts.SynthesizeSpeechRequest.newBuilder()
                .setAudioConfig(config)
                .setVoice(Tts.VoiceSelectionParams.newBuilder().setName(voice).build())
                .setInput(Tts.SynthesisInput.newBuilder().setText(inputText)).build();
        TtsStreamingSynthesisHandler responseHandler = new TtsStreamingSynthesisHandler();

        _clientTTS.streamingSynthesize(request, responseHandler);
        responseHandler.waitOnComplete();

        AudioParser.saveAudioInWAV(outputAudioPath, responseHandler.getAudioContent(), SYNTHESIS_SAMPLE_RATE);
    }

    void waitOnInput() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
