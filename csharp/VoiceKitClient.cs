using Tinkoff.Cloud.Stt.V1;
using Tinkoff.Cloud.Tts.V1;
using Grpc.Core;
using System.IO;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Threading;
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

        Metadata _getMetadataSTT
        {
            get
            {
                Metadata header = new Metadata();
                header.Add("Authorization", $"Bearer {_authSTT.GetToken}");
                return header;
            }
        }

        Metadata _getMetadataTTS
        {
            get
            {
                Metadata header = new Metadata();
                header.Add("Authorization", $"Bearer {_authTTS.GetToken}");
                return header;
            }
        }

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

        public string Rcognize(RecognitionConfig config, string path)
        {
            var audioBytes = File.ReadAllBytes(path);

            RecognizeRequest request = new RecognizeRequest();
            request.Config = config;
            request.Audio = new RecognitionAudio
            {
                Content = Google.Protobuf.ByteString.CopyFrom(audioBytes, 0, audioBytes.Length)
            };

            var response = _clientSTT.Recognize(request, this._getMetadataSTT);

            List<string> texts = new List<string>();

            foreach (var result in response.Results)
            {
                foreach (var alt in result.Alternatives)
                    texts.Add(alt.Transcript);
            }

            return string.Join(" ", texts);
        }

        public async Task StreamingRecognize(StreamingRecognitionConfig config, string path)
        {
            var stream = _clientSTT.StreamingRecognize(this._getMetadataSTT);
            var request = new StreamingRecognizeRequest();
            request.StreamingConfig = config;
            await stream.RequestStream.WriteAsync(request);

            Task PrintResponses = Task.Run(async () =>
            {
                while (await stream.ResponseStream.MoveNext(
                    default(CancellationToken)))
                {
                    foreach (var result in stream.ResponseStream
                        .Current.Results)
                    {
                        foreach (var alternative in result.RecognitionResult.Alternatives)
                        {
                            System.Console.WriteLine(alternative.Transcript);
                        }
                    }
                }
            });


            using (FileStream fileStream = new FileStream(path, FileMode.Open))
            {
                var buffer = new byte[2 * 1024];
                int bytesRead;
                while ((bytesRead = await fileStream.ReadAsync(buffer, 0, buffer.Length)) > 0)
                {
                    await stream.RequestStream.WriteAsync(
                    new StreamingRecognizeRequest()
                    {
                        AudioContent = Google.Protobuf.ByteString
                        .CopyFrom(buffer, 0, bytesRead),
                    });
                }
            }

            await stream.RequestStream.CompleteAsync();
            await PrintResponses;
        }

        public async Task StreamingSynthesize(string synthesizeInput, string audioName)
        {
            int sampleRate = 48000;
            var request = new SynthesizeSpeechRequest();
            var config = new AudioConfig
            {
                AudioEncoding = Tinkoff.Cloud.Tts.V1.AudioEncoding.Linear16,
                SampleRateHertz = sampleRate
            };

            request.AudioConfig = config;
            request.Input = new SynthesisInput
            {
                Text = synthesizeInput
            };

            var stream = _clientTTS.StreamingSynthesize(request, this._getMetadataTTS);

            List<Byte[]> audioBuffer = new List<Byte[]>();
            while (await stream.ResponseStream.MoveNext(
                    default(CancellationToken)))
            {
                audioBuffer.Add(stream.ResponseStream.Current.AudioChunk.ToByteArray());
            }

            var audioBytes = audioBuffer.SelectMany(byteArr => byteArr).ToArray();
            using (WaveFileWriter writer = new WaveFileWriter(audioName, new WaveFormat(sampleRate, 1)))
            {
                writer.Write(audioBytes, 0, audioBytes.Length);
            }
        }
    }
}