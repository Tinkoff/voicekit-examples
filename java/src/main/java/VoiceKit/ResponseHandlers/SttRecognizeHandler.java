package VoiceKit.ResponseHandlers;
import tinkoff.cloud.stt.v1.Stt;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SttRecognizeHandler extends BaseHandler<Stt.RecognizeResponse> {
    List<Stt.RecognizeResponse> _responses = new LinkedList<>();

    @Override
    public void onNext(Stt.RecognizeResponse value) {
        _responses.add(value);
    }

    public String getText() {
        return _responses.stream()
                .map(r -> r.getResults(0).getAlternatives(0).getTranscript())
                .collect(Collectors.joining(" "));
    }
}
