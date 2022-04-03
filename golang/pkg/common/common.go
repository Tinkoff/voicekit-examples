package common

import (
	"crypto/tls"
	"crypto/x509"
	"errors"
	"fmt"
	"io"
	"os"
	"strings"

	"github.com/Tinkoff/voicekit-examples/golang/pkg/args"
	"github.com/Tinkoff/voicekit-examples/golang/pkg/auth"
	sttPb "github.com/Tinkoff/voicekit-examples/golang/pkg/tinkoff/cloud/stt/v1"
	ttsPb "github.com/Tinkoff/voicekit-examples/golang/pkg/tinkoff/cloud/tts/v1"
	"github.com/go-audio/wav"
	"github.com/tidwall/pretty"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/protobuf/encoding/protojson"
	"google.golang.org/protobuf/proto"
)

func GetAuthorizationKeysFromEnv() (auth.KeyPair, error) {
	apiKey := os.Getenv("VOICEKIT_API_KEY")
	secretKey := os.Getenv("VOICEKIT_SECRET_KEY")

	if apiKey == "" || secretKey == "" {
		return auth.KeyPair{}, errors.New("No VOICEKIT_API_KEY or VOICEKIT_SECRET_KEY in env")
	}

	return auth.KeyPair{
		ApiKey:    apiKey,
		SecretKey: secretKey,
	}, nil
}

func isEndpointSecure(endpoint string) bool {
	parts := strings.Split(endpoint, ":")
	if len(parts) != 2 {
		return false
	}

	return parts[1] == "443"
}

func makeConnection(opts *args.CommonOptions, creds *auth.JwtPerRPCCredentials) (*grpc.ClientConn, error) {
	if isEndpointSecure(*opts.Endpoint) {
		var rootCAs *x509.CertPool
		if *opts.CAfile != "" {
			pemServerCA, err := os.ReadFile(*opts.CAfile)
			if err != nil {
				return nil, err
			}

			rootCAs = x509.NewCertPool()
			if !rootCAs.AppendCertsFromPEM(pemServerCA) {
				return nil, fmt.Errorf("failed to add server CA's certificate")
			}
		}

		return grpc.Dial(
			*opts.Endpoint,
			grpc.WithTransportCredentials(credentials.NewTLS(&tls.Config{
				RootCAs: rootCAs,
			})),
			grpc.WithPerRPCCredentials(creds),
		)
	}

	return grpc.Dial(
		*opts.Endpoint,
		grpc.WithTransportCredentials(insecure.NewCredentials()),
	)
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

func NewSttClient(opts *args.CommonOptions) (SpeechToTextClient, error) {
	keyPair, err := GetAuthorizationKeysFromEnv()
	if err != nil {
		return nil, err
	}

	perRPCCredentials := auth.NewJwtPerRPCCredentials(keyPair, "test_issuer", "test_subject")
	connection, err := makeConnection(opts, perRPCCredentials)

	return &speechToTextClient{
		SpeechToTextClient: sttPb.NewSpeechToTextClient(connection),
		conn:               connection,
	}, err
}

func NewTtsClient(opts *args.CommonOptions) (TextToSpeechClient, error) {
	keyPair, err := GetAuthorizationKeysFromEnv()
	if err != nil {
		return nil, err
	}

	perRPCCredentials := auth.NewJwtPerRPCCredentials(keyPair, "test_issuer", "test_subject")
	connection, err := makeConnection(opts, perRPCCredentials)

	return &textToSpeechClient{
		TextToSpeechClient: ttsPb.NewTextToSpeechClient(connection),
		conn:               connection,
	}, err
}

func PrettyPrintProtobuf(message proto.Message) error {
	marshaller := protojson.MarshalOptions{
		Indent: "  ",
	}
	jsonMessage, err := marshaller.Marshal(message)
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
		"LINEAR16": 0x0001,
		"ALAW":     0x0006,
		"MULAW":    0x0007,
	}
	encodingBitDepth := map[string]uint16{
		"LINEAR16": 16,
		"ALAW":     8,
		"MULAW":    8,
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
