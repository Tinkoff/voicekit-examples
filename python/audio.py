import abc
import contextlib
import struct
import wave

from tinkoff.cloud.stt.v1 import stt_pb2
from tinkoff.cloud.tts.v1 import tts_pb2


def _encoding_to_pyaudio_format_and_width(encoding):
    import pyaudio
    if encoding in [stt_pb2.LINEAR16, tts_pb2.LINEAR16]:
        return pyaudio.paInt16, 2
    else:
        raise ValueError("pyaudio: does not support encoding {}".format(encoding))


class AudioReader(contextlib.ExitStack, metaclass=abc.ABCMeta):
    @abc.abstractmethod
    def read(self):
        pass

    def read_all(self):
        all_data = bytearray()
        while True:
            data = self.read()
            if not data:
                break
            all_data.extend(data)
        return bytes(all_data)


class FileReader(AudioReader):
    def __init__(self, filename, max_chunk_size):
        super().__init__()
        self._filename = filename
        self._max_chunk_size = max_chunk_size

    def __enter__(self):
        self._file = self.enter_context(open(self._filename, "rb"))
        return super().__enter__()

    def read(self):
        return self._file.read(self._max_chunk_size)


class RawOpusReader(FileReader):
    def read(self):
        length_bytes = self._file.read(4)
        if not length_bytes:
            return None
        length = struct.unpack(">I", length_bytes)[0]
        return self._file.read(length)


class WaveReader(FileReader):
    def __init__(self, filename, max_chunk_size, encoding_hint, sample_rate_hint, num_channels_hint):
        super().__init__(filename, max_chunk_size)
        self._encoding_hint = encoding_hint
        self._sample_rate_hint = sample_rate_hint
        self._num_channels_hint = num_channels_hint

    def _check_wav_format(self, wav):
        if self._encoding_hint in [stt_pb2.MPEG_AUDIO, stt_pb2.ALAW, stt_pb2.MULAW]:
            raise ValueError("Unsupported encoding {} for WAV".format(stt_pb2.AudioEncoding.Name(self._encoding_hint)))
        if wav.getnchannels() != self._num_channels_hint:
            raise ValueError("Channels detected: {} vs specified: {}".format(wav.getnchannels(),
                                                                             self._num_channels_hint))
        if wav.getframerate() != self._sample_rate_hint:
            raise ValueError("Sample rate detected: {} vs specified: {}".format(wav.getframerate(),
                                                                                self._sample_rate_hint))
        if wav.getsampwidth() != 2 and self._encoding_hint == stt_pb2.LINEAR16:
            raise ValueError("Specified encoding LINEAR16, got sample width: {}".format(wav.getsampwidth()))

    def __enter__(self):
        ret = super().__enter__()
        self._wav = self.enter_context(wave.open(self._file))
        self._check_wav_format(self._wav)
        return ret

    def read(self):
        return self._wav.readframes(self._max_chunk_size // (self._wav.getnchannels() * self._wav.getsampwidth()))


class PyAudioReader(AudioReader):
    def __init__(self, encoding_hint, sample_rate_hint, num_channels_hint, max_chunk_size, pyaudio_max_seconds=None):
        super().__init__()
        self._encoding_hint = encoding_hint
        self._sample_rate_hint = sample_rate_hint
        self._num_channels_hint = num_channels_hint
        self._max_chunk_size = max_chunk_size
        self._pyaudio_max_seconds = pyaudio_max_seconds
        self._total_seconds = 0.0

    def __enter__(self):
        import pyaudio
        pyaudio_lib = pyaudio.PyAudio()
        self.callback(pyaudio_lib.terminate)
        format, self._width = _encoding_to_pyaudio_format_and_width(self._encoding_hint)
        self._stream = pyaudio_lib.open(input=True, channels=self._num_channels_hint, format=format,
                                        rate=self._sample_rate_hint)
        self.callback(self._stream.close)
        self.callback(self._stream.stop_stream)
        return super().__enter__()

    def read(self):
        if self._pyaudio_max_seconds and self._total_seconds > self._pyaudio_max_seconds:
            return None
        data = self._stream.read(self._max_chunk_size)
        self._total_seconds += len(data) / (self._sample_rate_hint * self._num_channels_hint * self._width)
        return data


class AudioWriter(contextlib.ExitStack, metaclass=abc.ABCMeta):
    OPUS_MAX_FRAME_SIZE = 5760

    @abc.abstractmethod
    def write(self, data: bytes):
        pass


class RawOpusWriter(AudioWriter):
    def __init__(self, filename):
        super().__init__()
        self._filename = filename

    def __enter__(self):
        self._file = self.enter_context(open(self._filename, "wb"))
        return super().__enter__()

    def write(self, data: bytes):
        self._file.write(struct.pack(">I", len(data)))
        self._file.write(data)


class WaveWriter(AudioWriter):
    def __init__(self, filename, encoding_hint, sample_rate_hint):
        super().__init__()
        if encoding_hint not in [tts_pb2.LINEAR16, tts_pb2.RAW_OPUS]:
            raise ValueError("Only LINEAR16 audio is supported")
        self._filename = filename
        self._encoding_hint = encoding_hint
        self._sample_rate_hint = sample_rate_hint

    def __enter__(self):
        self._wav = self.enter_context(wave.open(self._filename, "wb"))
        self._wav.setframerate(self._sample_rate_hint)
        self._wav.setnchannels(1)
        self._wav.setsampwidth(2)
        if self._encoding_hint == tts_pb2.RAW_OPUS:
            import opuslib
            self._opus_decoder = opuslib.Decoder(self._sample_rate_hint, 1)
        else:
            self._opus_decoder = None
        return super().__enter__()

    def write(self, data: bytes):
        if self._opus_decoder is not None:
            data = self._opus_decoder.decode(data, self.OPUS_MAX_FRAME_SIZE, 0)
        self._wav.writeframes(data)


class PyAudioWriter(AudioWriter):
    def __init__(self, encoding_hint, sample_rate_hint):
        super().__init__()
        self._encoding_hint = encoding_hint
        self._sample_rate_hint = sample_rate_hint

    def __enter__(self):
        if self._encoding_hint == tts_pb2.RAW_OPUS:
            import opuslib
            self._encoding_hint = tts_pb2.LINEAR16
            self._opus_decoder = opuslib.Decoder(self._sample_rate_hint, 1)
        else:
            self._opus_decoder = None
        import pyaudio
        pyaudio_lib = pyaudio.PyAudio()
        self.callback(pyaudio_lib.terminate)
        format, self._width = _encoding_to_pyaudio_format_and_width(self._encoding_hint)
        self._stream = pyaudio_lib.open(output=True, channels=1, format=format, rate=self._sample_rate_hint)
        self.callback(self._stream.close)
        self.callback(self._stream.stop_stream)
        return super().__enter__()

    def write(self, data: bytes):
        if self._opus_decoder is not None:
            data = self._opus_decoder.decode(data, self.OPUS_MAX_FRAME_SIZE, 0)
        return self._stream.write(data)


def audio_open_read(filename: str, encoding_hint: stt_pb2.AudioEncoding, sample_rate_hint: int,
                    num_channels_hint: int, max_chunk_size: int, pyaudio_max_seconds: int):
    if filename.startswith("pyaudio:"):
        return PyAudioReader(encoding_hint, sample_rate_hint, num_channels_hint, max_chunk_size,
                             pyaudio_max_seconds)
    elif encoding_hint == stt_pb2.RAW_OPUS:
        return RawOpusReader(filename, max_chunk_size)
    elif filename.endswith(".wav"):
        return WaveReader(filename, max_chunk_size, encoding_hint, sample_rate_hint, num_channels_hint)
    else:
        return FileReader(filename, max_chunk_size)


def audio_open_write(filename: str, encoding_hint: tts_pb2.AudioEncoding, sample_rate_hint: int):
    if filename.startswith("pyaudio:"):
        return PyAudioWriter(encoding_hint, sample_rate_hint)
    elif filename.endswith(".raw_opus") and encoding_hint == tts_pb2.RAW_OPUS:
        return RawOpusWriter(filename)
    else:
        return WaveWriter(filename, encoding_hint, sample_rate_hint)
