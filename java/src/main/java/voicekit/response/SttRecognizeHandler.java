package voicekit.response;
import com.google.protobuf.InvalidProtocolBufferException;
import tinkoff.cloud.stt.v1.Stt;

import com.google.protobuf.util.JsonFormat;
import voicekit.utils.Printer;

public class SttRecognizeHandler extends BaseHandler<Stt.RecognizeResponse> {

    @Override
    public void onNext(Stt.RecognizeResponse value) {
        try {
            Printer.getPrinter().println(JsonFormat.printer().print(value));
        } catch (InvalidProtocolBufferException e) {
            onError(e);
        }
    }
}
