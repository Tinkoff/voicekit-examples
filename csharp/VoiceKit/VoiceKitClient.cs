using Tinkoff.Cloud.Stt.V1;
using Tinkoff.Cloud.Tts.V1;
using Grpc.Core;
using System.IO;
using System.Collections.Generic;
using System.Threading.Tasks;
using System;
using NAudio.Wave;
using System.Linq;

namespace Tinkoff.VoiceKit
{
    public class VoiceKitClient
    {
        SpeechToText.SpeechToTextClient _clientSTT;
        TextToSpeech.TextToSpeechClient _clientTTS;
        Auth _authSTT;
        Auth _authTTS;

        public VoiceKitClient(string apiKey, string secretKey)
        {
            _authSTT = new Auth(apiKey, secretKey, "tinkoff.cloud.stt");
            _authTTS = new Auth(apiKey, secretKey, "tinkoff.cloud.tts");

            var cred = new SslCredentials();
            var channelSTT = new Channel("stt.tinkoff.ru:443", cred);
            var channelTTS = new Channel("tts.tinkoff.ru:443", cred);

            _clientSTT = new SpeechToText.SpeechToTextClient(channelSTT);
            _clientTTS = new TextToSpeech.TextToSpeechClient(channelTTS);
        }

        private Metadata GetMetadataSTT()
        {
            Metadata header = new Metadata();
            header.Add("Authorization", $"Bearer {_authSTT.Token}");
            return header;
        }

        private Metadata GetMetadataTTS()
        {
            Metadata header = new Metadata();
            header.Add("Authorization", $"Bearer {_authTTS.Token}");
            return header;
        }

        public string Recognize(RecognitionConfig config, Stream audioStream)
        {
            byte[] audioBytes;
            using (MemoryStream buffer = new MemoryStream())
            {
                audioStream.CopyTo(buffer);
                audioBytes = buffer.ToArray();
            }

            RecognizeRequest request = new RecognizeRequest();
            request.Config = config;
            request.Audio = new RecognitionAudio
            {
                Content = Google.Protobuf.ByteString.CopyFrom(audioBytes, 0, audioBytes.Length)
            };

            var response = _clientSTT.Recognize(request, this.GetMetadataSTT());

            var texts = new List<string>();

            foreach (var result in response.Results)
            {
                foreach (var alt in result.Alternatives)
                    texts.Add(alt.Transcript);
            }

            return string.Join(" ", texts);
        }

        public async Task StreamingRecognize(StreamingRecognitionConfig config, Stream audioStream)
        {
            var streamingSTT = _clientSTT.StreamingRecognize(GetMetadataSTT());
            var requestWithConfig = new StreamingRecognizeRequest
            {
                StreamingConfig = config
            };
            await streamingSTT.RequestStream.WriteAsync(requestWithConfig);

            Task PrintResponsesTask = Task.Run(async () =>
            {
                while (await streamingSTT.ResponseStream.MoveNext())
                {
                    foreach (var result in streamingSTT.ResponseStream.Current.Results)
                        foreach (var alternative in result.RecognitionResult.Alternatives)
                            System.Console.WriteLine(alternative.Transcript);
                }
            });

            var buffer = new byte[2 * 1024];
            int bytesRead;
            while ((bytesRead = audioStream.Read(buffer, 0, buffer.Length)) > 0)
            {
                await streamingSTT.RequestStream.WriteAsync(
                    new StreamingRecognizeRequest
                    {
                        AudioContent = Google.Protobuf
                        .ByteString.CopyFrom(buffer, 0, bytesRead),
                    });
            }

            await streamingSTT.RequestStream.CompleteAsync();
            await PrintResponsesTask;
        }

        public async Task StreamingSynthesize(string synthesizeInput, string audioName)
        {
            int sampleRate = 48000;
            var request = new SynthesizeSpeechRequest
            {
                AudioConfig = new AudioConfig
                {
                    AudioEncoding = Tinkoff.Cloud.Tts.V1.AudioEncoding.Linear16,
                    SampleRateHertz = sampleRate
                },
                Input = new SynthesisInput
                {
                    Text = synthesizeInput
                } 
            };

            var stream = _clientTTS.StreamingSynthesize(request, GetMetadataTTS());

            var audioBuffer = new List<Byte[]>();
            while (await stream.ResponseStream.MoveNext())
            {
                audioBuffer.Add(stream.ResponseStream.Current.AudioChunk.ToByteArray());
            }

            var audioBytes = audioBuffer.SelectMany(byteArr => byteArr).ToArray();
            using (var writer = new WaveFileWriter(audioName, new WaveFormat(sampleRate, 1)))
            {
                writer.Write(audioBytes, 0, audioBytes.Length);
            }
        }
    }
}