package voicekit.utils;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class AudioParser {
    private AudioParser() {
        throw new IllegalStateException("Utility class");
    }

    static AudioFormat getAudioFormat(int sampleRate) {
        return new AudioFormat(sampleRate, 16, 1, true, false);
    }

    public static TargetDataLine getMicrophoneStream() throws LineUnavailableException {
        AudioFormat format = getAudioFormat(16000);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            return null;
        }
        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        return line;
    }

    public static void saveAudioInWAV(String outputAudioPath, byte[] audio, int sampleRate) throws IOException {
        File out = new File(outputAudioPath);
        AudioFormat format = getAudioFormat(sampleRate);
        try(ByteArrayInputStream audioStream = new ByteArrayInputStream(audio);
            AudioInputStream audioInputStream = new AudioInputStream(audioStream, format, audio.length)) {
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, out);
        }
    }
}
