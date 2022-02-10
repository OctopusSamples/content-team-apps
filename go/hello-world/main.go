package main

import (
	"context"
	"encoding/json"
	"github.com/aws/aws-lambda-go/events"
	"github.com/aws/aws-lambda-go/lambda"
)

func HandleRequest(ctx context.Context, input map[string]interface{}) (events.APIGatewayProxyResponse, error) {
	inputJson, _ := json.Marshal(input)
	return events.APIGatewayProxyResponse{
		StatusCode: 200,
		Body:       string(inputJson),
		Headers:    map[string]string{"Access-Control-Allow-Origin": "*"},
	}, nil
}

func main() {
	lambda.Start(HandleRequest)
}