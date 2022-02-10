package main

import (
	"github.com/OctopusSamples/OctoPub/go/reverse-proxy/internal/pkg/lambdahandler"
	"github.com/aws/aws-lambda-go/lambda"
)

func main() {
	lambda.Start(lambdahandler.HandleRequest)
}
