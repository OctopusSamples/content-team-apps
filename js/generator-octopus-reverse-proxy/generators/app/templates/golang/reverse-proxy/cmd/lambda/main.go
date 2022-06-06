package main

import (
	"github.com/OctopusSamples/content-team-apps/go/reverse-proxy/internal/pkg/lambdahandler"
	"github.com/aws/aws-lambda-go/lambda"
)

func main() {
	lambda.Start(lambdahandler.HandleRequest)
}
