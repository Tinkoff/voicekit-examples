package infrastructure.cli;

public class Params {

    private Params() {
        throw new IllegalStateException("Data structure class");
    }

    public static final String AUDIO_PATH = "audio-path";
    public static final String SAMPLE_RATE = "sample-rate";
    public static final String CHANNELS_COUNT = "channels-count";
    public static final String ENCODING = "audio-encoding";
    public static final String MAX_ALTERNATIVES = "max-alternatives";
    public static final String DO_NOT_PERFORM_VAD = "do-not-perform-vad";
    public static final String SILENCE_DURATION_THRESHOLD = "silence-duration-threshold";
    public static final String DISABLE_AUTOMATIC_PUNCTUATION = "disable-automatic-punctuation";

    public static final String ENABLE_INTERIM_RESULTS = "enable-interim-results";
    public static final String SINGLE_UTTERANCE = "single-utterance";

    public static final String TEXT = "text";
    public static final String OUTPUT = "output";
    public static final String VOICE = "voice";
}
