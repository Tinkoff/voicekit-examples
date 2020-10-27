package VoiceKit.ResponseHandlers;
import VoiceKit.Utils.Printer;
import tinkoff.cloud.stt.v1.Stt;

import java.util.List;

public class SttStreamingRecognizeHandler extends BaseHandler<Stt.StreamingRecognizeResponse> {

    @Override
    public void onNext(Stt.StreamingRecognizeResponse value) {
        List<Stt.SpeechRecognitionAlternative> alternativeList = value.getResults(0)
                .getRecognitionResult().getAlternativesList();

        for (Stt.SpeechRecognitionAlternative alter: alternativeList)
            Printer.getPrinter().println(alter.getTranscript());
    }
}
