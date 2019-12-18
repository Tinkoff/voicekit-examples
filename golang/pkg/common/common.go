package common

import (
	"crypto/tls"
	"errors"
	"fmt"
	"github.com/TinkoffCreditSystems/voicekit-examples/golang/pkg/auth"
	sttPb "github.com/TinkoffCreditSystems/voicekit-examples/golang/pkg/tinkoff/cloud/stt/v1"
	ttsPb "github.com/TinkoffCreditSystems/voicekit-examples/golang/pkg/tinkoff/cloud/tts/v1"
	"github.com/go-audio/wav"
	"github.com/golang/protobuf/jsonpb"
	"github.com/golang/protobuf/proto"
	"github.com/tidwall/pretty"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"io"
	"os"
)

func GetAuthorizationKeysFromEnv() (auth.KeyPair, error) {
	apiKey := os.Getenv("VOICEKIT_API_KEY")
	secretKey := os.Getenv("VOICEKIT_SECRET_KEY")

	if apiKey == "" || secretKey == "" {
		return auth.KeyPair{}, errors.New("No VOICEKIT_API_KEY or VOICEKIT_SECRET_KEY in env")
	}

	return auth.KeyPair{
		ApiKey: apiKey,
		SecretKey: secretKey,
	}, nil
}


type SpeechToTextClient interface {
	sttPb.SpeechToTextClient
	Close() error
}

type TextToSpeechClient interface {
	ttsPb.TextToSpeechClient
	Close() error
}

type speechToTextClient struct {
	sttPb.SpeechToTextClient
	conn *grpc.ClientConn
}

type textToSpeechClient struct {
	ttsPb.TextToSpeechClient
	conn *grpc.ClientConn
}

func (client *speechToTextClient) Close() error {
	return client.conn.Close()
}

func (client *textToSpeechClient) Close() error {
	return client.conn.Close()
}

func NewSttClient() (SpeechToTextClient, error) {
	keyPair, err := GetAuthorizationKeysFromEnv()
	if err != nil {
		return nil, err
	}

	transportCredentials := credentials.NewTLS(&tls.Config{})
	perRPCCredentials := auth.NewJwtPerRPCCredentials(keyPair, "test_issuer", "test_subject")

	connection, err := grpc.Dial("stt.tinkoff.ru:443",
		grpc.WithTransportCredentials(transportCredentials),
		grpc.WithPerRPCCredentials(perRPCCredentials))

	return &speechToTextClient{
		SpeechToTextClient: sttPb.NewSpeechToTextClient(connection),
		conn: connection,
	}, err
}

func NewTtsClient() (TextToSpeechClient, error) {
	keyPair, err := GetAuthorizationKeysFromEnv()
	if err != nil {
		return nil, err
	}

	transportCredentials := credentials.NewTLS(&tls.Config{})
	perRPCCredentials := auth.NewJwtPerRPCCredentials(keyPair, "test_issuer", "test_subject")

	connection, err := grpc.Dial("tts.tinkoff.ru:443",
		grpc.WithTransportCredentials(transportCredentials),
		grpc.WithPerRPCCredentials(perRPCCredentials))

	return &textToSpeechClient{
		TextToSpeechClient: ttsPb.NewTextToSpeechClient(connection),
		conn: connection,
	}, err
}

func PrettyPrintProtobuf(message proto.Message) error {
	marshaller := &jsonpb.Marshaler{
		Indent:       "  ",
	}
	jsonMessage, err := marshaller.MarshalToString(message)
	if err != nil {
		return err
	}

	fmt.Println(string(pretty.Color([]byte(jsonMessage), pretty.TerminalStyle)))
	return nil
}

func OpenWavFormat(file *os.File, expectedEncoding string, expectedNumChannels int, expectedRate int) (io.Reader, error) {
	wavDecoder := wav.NewDecoder(file)
	wavDecoder.ReadInfo()

	encodingAudioFormat := map[string]uint16{
		"LINEAR16":  0x0001,
		"ALAW":      0x0006,
		"MULAW":     0x0007,
	}
	encodingBitDepth := map[string]uint16{
		"LINEAR16":  16,
		"ALAW":      8,
		"MULAW":     8,
	}
	if encodingAudioFormat[expectedEncoding] != wavDecoder.WavAudioFormat {
		return nil, fmt.Errorf("bad audio format, expected %s, found %v", expectedEncoding, wavDecoder.WavAudioFormat)
	}
	if encodingBitDepth[expectedEncoding] != wavDecoder.BitDepth {
		return nil, fmt.Errorf("expected bid depth %v, but found %v", encodingBitDepth[expectedEncoding], wavDecoder.BitDepth)
	}
	if expectedNumChannels != int(wavDecoder.NumChans) {
		return nil, fmt.Errorf("expected %v channels, but found %v", expectedNumChannels, wavDecoder.NumChans)
	}
	if expectedRate != int(wavDecoder.SampleRate) {
		return nil, fmt.Errorf("expected %v sample rate, but found %v", expectedRate, wavDecoder.SampleRate)
	}

	if wavDecoder.FwdToPCM() != nil {
		return nil, fmt.Errorf("forwarding to data chunk failed")
	}
	return wavDecoder.PCMChunk.R, nil
}
