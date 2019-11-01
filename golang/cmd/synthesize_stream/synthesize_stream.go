package main

import (
	"context"
	"encoding/binary"
	"fmt"
	"github.com/TinkoffCreditSystems/voicekit-examples/golang/pkg/args"
	"github.com/TinkoffCreditSystems/voicekit-examples/golang/pkg/common"
	"gopkg.in/hraban/opus.v2"
	ttsPb "github.com/TinkoffCreditSystems/voicekit-examples/golang/pkg/tinkoff/cloud/tts/v1"
	"github.com/go-audio/audio"
	"github.com/go-audio/wav"
	"io"
	"os"
)

const (
	kAudioFormatPCM   = 1
	kBitDepthLinear16 = 16
	kMonoNumChannels  = 1
	kOpusMaxFrameSize = 5760
)

func main() {
	opts := args.ParseStreamingSynthesizeOptions()
	if opts == nil {
		os.Exit(1)
	}
	if *opts.Encoding == "LINEAR16" && *opts.Rate != 48000 {
		fmt.Println("LINEAR16 only supports 48 kHz for now.")
		os.Exit(1)
	}

	defer opts.OutputFile.Close()

	encoder := wav.NewEncoder(opts.OutputFile, *opts.Rate, kBitDepthLinear16, kMonoNumChannels, kAudioFormatPCM)
	defer encoder.Close()

	client, err := common.NewTtsClient()
	if err != nil {
		panic(err)
	}
	defer client.Close()

	// NOTE: in production code you should probably use context.WithCancel, context.WithDeadline or context.WithTimeout
	// instead of context.Background()
	stream, err := client.StreamingSynthesize(context.Background(), &ttsPb.SynthesizeSpeechRequest{
		Input: &ttsPb.SynthesisInput{
			Text: *opts.InputText,
		},
		AudioConfig: &ttsPb.AudioConfig{
			AudioEncoding:   ttsPb.AudioEncoding(ttsPb.AudioEncoding_value[*opts.Encoding]),
			SampleRateHertz: int32(*opts.Rate),
		},
	})
	if err != nil {
		panic(err)
	}
	var decoder *opus.Decoder
	if *opts.Encoding == "RAW_OPUS" {
		decoder, err = opus.NewDecoder(*opts.Rate, kMonoNumChannels)
		if err != nil {
			panic(err)
		}
	}

	startedStreaming := false
	for {
		msg, err := stream.Recv()
		if err == io.EOF {
			break
		}
		if err != nil {
			panic(err)
		}
		if !startedStreaming {
			startedStreaming = true
			fmt.Println("Started streaming")
		}

		samples := make([]int, 0)
		if *opts.Encoding == "RAW_OPUS" {
			pcmBuffer := make([]int16, kOpusMaxFrameSize)
			numSamples, err := decoder.Decode(msg.AudioChunk, pcmBuffer)
			if err != nil {
				panic(err)
			}
			for i := 0; i < numSamples; i++ {
				samples = append(samples, int(pcmBuffer[i]))
			}
		} else {
			elementSize := kBitDepthLinear16 / 8
			for i := 0; i < len(msg.AudioChunk); i += elementSize {
				samples = append(samples, int(int16(binary.LittleEndian.Uint16(msg.AudioChunk[i : i + elementSize]))))
			}
		}

		if err := encoder.Write(&audio.IntBuffer{
			Format: &audio.Format{
				NumChannels: kMonoNumChannels,
				SampleRate:  *opts.Rate,
			},
			Data:           samples,
			SourceBitDepth: kBitDepthLinear16,
		}); err != nil {
			panic(err)
		}
	}

}
