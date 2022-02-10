package utils

import (
	"context"
	"errors"
	"fmt"
	"github.com/dgrijalva/jwt-go"
	"github.com/lestrrat-go/jwx/jwa"
	"github.com/lestrrat-go/jwx/jwk"
	"os"
)

type CognitoClaims struct {
	Audience      string   `json:"aud,omitempty"`
	ExpiresAt     int64    `json:"exp,omitempty"`
	Id            string   `json:"jti,omitempty"`
	IssuedAt      int64    `json:"iat,omitempty"`
	Issuer        string   `json:"iss,omitempty"`
	NotBefore     int64    `json:"nbf,omitempty"`
	Subject       string   `json:"sub,omitempty"`
	CognitoGroups []string `json:"cognito:groups,omitempty"`
}

func (c CognitoClaims) Valid() error {
	claims := jwt.StandardClaims{
		Audience:  c.Audience,
		ExpiresAt: c.ExpiresAt,
		Id:        c.Id,
		IssuedAt:  c.IssuedAt,
		Issuer:    c.Issuer,
		NotBefore: c.NotBefore,
		Subject:   c.Subject,
	}

	return claims.Valid()
}

// GetEnv returns the environment variable, or if it is not defined returns the fallback
func GetEnv(key, fallback string) string {
	if value, ok := os.LookupEnv(key); ok {
		return value
	}
	return fallback
}

func VerifyJwt(tokenString string, jwkUrl string) (*jwt.Token, error) {
	keySet, err := jwk.Fetch(context.Background(), jwkUrl)

	if err != nil {
		return nil, err
	}

	return jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
		if token.Method.Alg() != jwa.RS256.String() {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}
		kid, ok := token.Header["kid"].(string)
		if !ok {
			return nil, errors.New("kid header not found")
		}
		key, success := keySet.LookupKeyID(kid)
		if !success {
			return nil, fmt.Errorf("key %v not found", kid)
		}
		var raw interface{}
		return raw, key.Raw(&raw)
	})
}
