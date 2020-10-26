package VoiceKit.ResponseHandlers;

import tinkoff.cloud.tts.v1.Tts;

import java.util.LinkedList;

public class TtsStreamingSynthesisHandler extends BaseHandler<Tts.StreamingSynthesizeSpeechResponse> {
    LinkedList<byte[]> _audioBytes = new LinkedList<>();

    @Override
    public void onNext(Tts.StreamingSynthesizeSpeechResponse value) {
        _audioBytes.add(value.getAudioChunk().toByteArray());
    }

    public byte[] getAudioContent() {
        int totalLength = _audioBytes.stream().map(bytes -> bytes.length).reduce(0, Integer::sum);
        byte[] audio = new byte[totalLength];

        int i=0;
        for(byte[] bytes: _audioBytes) {
            for (byte b : bytes)
                audio[i++] = b;
        }

        return audio;
    }
}
