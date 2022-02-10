package lambdahandler_test

import (
	"context"
	"github.com/OctopusSamples/OctoPub/go/reverse-proxy/internal/pkg/lambdahandler"
	"github.com/aws/aws-lambda-go/events"
	"github.com/stretchr/testify/assert"
	"os"
	"testing"
)

func TestHandler(t *testing.T) {

	accessToken := os.Getenv("TEST_ACCESS_TOKEN")
	if accessToken != "" {
		accessToken = "Bearer: " + accessToken
	}

	tests := []struct {
		request events.APIGatewayProxyRequest
		expect  string
		err     error
	}{
		{
			request: events.APIGatewayProxyRequest{
				Headers: map[string]string{
					"Accept":        "application/vnd.api+json,application/vnd.api+json; version[/api/products*:GET]=lambda[" + os.Getenv("TEST_LAMBDA") + "]",
					"Host":          "localhost",
					"Authorization": accessToken,
				},
				Path:       "/api/products",
				HTTPMethod: "GET",
			},
			expect: "application/vnd.api+json,application/vnd.api+json; version[/api/products*:GET]=lambda[" + os.Getenv("TEST_LAMBDA") + "]",
			err:    nil,
		},
		{
			request: events.APIGatewayProxyRequest{
				Body: "My Request",
				Headers: map[string]string{
					"Accept":        "application/vnd.api+json,application/vnd.api+json; version[/post*:POST]=url[https://postman-echo.com]",
					"Host":          "localhost",
					"Authorization": accessToken,
				},
				Path:       "/post",
				HTTPMethod: "POST",
			},
			expect: "My Request",
			err:    nil,
		},
		{
			request: events.APIGatewayProxyRequest{
				Body: "My Request",
				Headers: map[string]string{
					"Accept":        "application/vnd.api+json,application/vnd.api+json; version[/post*:GET]=url[https://postman-echo.com]; version[/post*:POST]=path[/post*:GET]",
					"Host":          "localhost",
					"Authorization": accessToken,
				},
				Path:       "/post",
				HTTPMethod: "POST",
			},
			expect: "My Request",
			err:    nil,
		},
		{
			request: events.APIGatewayProxyRequest{
				Body: "My Request",
				Headers: map[string]string{
					"Accept":        "application/vnd.api+json,application/vnd.api+json; version[/put*:PUT]=url[https://postman-echo.com]",
					"Host":          "localhost",
					"Authorization": accessToken,
				},
				Path:       "/put",
				HTTPMethod: "PUT",
			},
			expect: "My Request",
			err:    nil,
		},
		{
			request: events.APIGatewayProxyRequest{
				Headers: map[string]string{
					"Accept":        "application/vnd.api+json,application/vnd.api+json; version[/get*:GET]=url[https://postman-echo.com]",
					"Host":          "localhost",
					"Authorization": accessToken,
				},
				Path:       "/get",
				HTTPMethod: "GET",
			},
			expect: "https://postman-echo.com/",
			err:    nil,
		},
		{
			request: events.APIGatewayProxyRequest{
				Headers: map[string]string{
					"Accept":        "application/vnd.api+json,application/vnd.api+json; version[/common:GET]=url[https://postman-echo.com]; version[/get*:GET]=path[/common:GET]",
					"Host":          "localhost",
					"Authorization": accessToken,
				},
				Path:       "/get",
				HTTPMethod: "GET",
			},
			expect: "https://postman-echo.com/",
			err:    nil,
		},
		{
			request: events.APIGatewayProxyRequest{
				Body: "My Body",
				Headers: map[string]string{
					"Accept":        "application/vnd.api+json,application/vnd.api+json; version[/common:POST]=sqs[" + os.Getenv("TEST_SQS") + "]",
					"Host":          "localhost",
					"Authorization": accessToken,
				},
				Path:       "/common",
				HTTPMethod: "POST",
			},
			expect: "",
			err:    nil,
		},
	}

	for _, test := range tests {
		ctx := context.Background()
		response, err := lambdahandler.HandleRequest(ctx, test.request)
		assert.IsType(t, test.err, err)
		assert.Contains(t, response.Body, test.expect)
		assert.Contains(t, response.Headers["Host"], "")
	}

}
