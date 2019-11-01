package auth

import (
	"context"
	"encoding/base64"
	"github.com/dgrijalva/jwt-go"
	"strings"
	"time"
)

type KeyPair struct {
	ApiKey    string
	SecretKey string
}

func generateJwt(keyPair KeyPair, claims *jwt.StandardClaims) (string, error) {
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	token.Header["kid"] = keyPair.ApiKey
	secretData, err := base64.StdEncoding.DecodeString(keyPair.SecretKey)
	if err != nil {
		return "", err
	}
	return token.SignedString(secretData)
}

type JwtPerRPCCredentials struct {
	keyPair   KeyPair
	issuer    string
	subject   string
}

func NewJwtPerRPCCredentials(keyPair KeyPair, issuer, subject string) *JwtPerRPCCredentials {
	return &JwtPerRPCCredentials{
		keyPair:   keyPair,
		issuer:    issuer,
		subject:   subject,
	}
}

func (creds *JwtPerRPCCredentials) GetRequestMetadata(ctx context.Context, uri ...string) (map[string]string, error) {
	uriParts := strings.Split(uri[0], "/")
	scope := uriParts[len(uriParts) - 1]
	claims := &jwt.StandardClaims{
		Audience:  scope,
		ExpiresAt: time.Now().Unix() + 600,
		Issuer:    creds.issuer,
		Subject:   creds.subject,
	}

	tokenString, err := generateJwt(creds.keyPair, claims)
	if err != nil {
		return nil, err
	}
	headers := make(map[string]string)
	headers["Authorization"] = "Bearer " + tokenString
	return headers, nil
}

func (*JwtPerRPCCredentials) RequireTransportSecurity() bool {
	return true
}
