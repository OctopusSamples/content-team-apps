module github.com/OctopusSamples/OctoPub/go/reverse-proxy

go 1.17

require (
	github.com/aws/aws-lambda-go v1.27.0
	github.com/aws/aws-sdk-go v1.42.22
	github.com/awslabs/aws-lambda-go-api-proxy v0.11.0
	github.com/dgrijalva/jwt-go v3.2.0+incompatible
	github.com/lestrrat-go/jwx v1.2.18
	github.com/stretchr/testify v1.7.0
	github.com/vibrantbyte/go-antpath v1.1.1
)

require (
	github.com/davecgh/go-spew v1.1.1 // indirect
	github.com/decred/dcrd/dcrec/secp256k1/v4 v4.0.0-20210816181553-5444fa50b93d // indirect
	github.com/goccy/go-json v0.9.4 // indirect
	github.com/jmespath/go-jmespath v0.4.0 // indirect
	github.com/lestrrat-go/backoff/v2 v2.0.8 // indirect
	github.com/lestrrat-go/blackmagic v1.0.0 // indirect
	github.com/lestrrat-go/httpcc v1.0.0 // indirect
	github.com/lestrrat-go/iter v1.0.1 // indirect
	github.com/lestrrat-go/option v1.0.0 // indirect
	github.com/pkg/errors v0.9.1 // indirect
	github.com/pmezard/go-difflib v1.0.0 // indirect
	golang.org/x/crypto v0.0.0-20201217014255-9d1352758620 // indirect
	gopkg.in/yaml.v3 v3.0.0-20200615113413-eeeca48fe776 // indirect
)

// +heroku goVersion 1.17
// +heroku install ./cmd/web/...
