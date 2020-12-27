package voicekit.response;

import tinkoff.cloud.tts.v1.Tts;

import java.util.LinkedList;

public class TtsStreamingSynthesisHandler extends BaseHandler<Tts.StreamingSynthesizeSpeechResponse> {
    LinkedList<byte[]> audioBytes = new LinkedList<>();

    @Override
    public void onNext(Tts.StreamingSynthesizeSpeechResponse value) {
        audioBytes.add(value.getAudioChunk().toByteArray());
    }

    public byte[] getAudioContent() {
        int totalLength = audioBytes.stream().map(bytes -> bytes.length).reduce(0, Integer::sum);
        byte[] audio = new byte[totalLength];

        int i=0;
        for(byte[] bytes: audioBytes) {
            for (byte b : bytes)
                audio[i++] = b;
        }

        return audio;
    }
}
