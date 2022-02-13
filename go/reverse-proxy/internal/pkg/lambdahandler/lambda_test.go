package lambdahandler_test

import (
	"context"
	"github.com/OctopusSamples/content-team-apps/go/reverse-proxy/internal/pkg/lambdahandler"
	"github.com/OctopusSamples/content-team-apps/go/reverse-proxy/internal/pkg/utils"
	"github.com/aws/aws-lambda-go/events"
	"github.com/stretchr/testify/assert"
	"os"
	"testing"
)

const routingHeader = "Routing"
const expiredToken = "eyJraWQiOiJUemtBZlNIakQxWEl0VFYzWFwvWEtlaTZTVHRVSHcwemdqYzhOQjVnM1J4QT0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiI5ZGE4ODM3ZS00YzYyLTQzZDgtYWVjMy0yYmJiOGNiYTFjZDgiLCJjb2duaXRvOmdyb3VwcyI6WyJEZXZlbG9wZXJzIiwidXMtd2VzdC0xX1ZrQWZuenFaRl9Hb29nbGUiXSwidG9rZW5fdXNlIjoiYWNjZXNzIiwic2NvcGUiOiJvcGVuaWQgcHJvZmlsZSBlbWFpbCIsImF1dGhfdGltZSI6MTY0NDUzMDU3MywiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLnVzLXdlc3QtMS5hbWF6b25hd3MuY29tXC91cy13ZXN0LTFfVmtBZm56cVpGIiwiZXhwIjoxNjQ0NTM0MTczLCJpYXQiOjE2NDQ1MzA1NzQsInZlcnNpb24iOjIsImp0aSI6ImVmOWM4Zjk0LWFiNzAtNDgwOS04ZjI5LTk2NjdlZTVmNjg1ZiIsImNsaWVudF9pZCI6IjUydHRrcXFsN3JsaHRwYWR2b2pqcDc4bGwzIiwidXNlcm5hbWUiOiJnb29nbGVfMTAyNjAzOTY1MzQzNTAzMjY0MDc5In0.yIJsqghuDUTgFzyZJlEBTRd1OblBxhu9cYGNmTh87d9UpeEJ3bxYTyojRZ_fWzhRwj7gllZsc1fcJRCxEEd-wotnMA13k_f2dnodLP2mgbkKPAGkRll1yVopT1nj21GmG287Tx9GV4RYoPhv9BjNukFyP45NkNnUTTQF144KX58gOxyJHQJZ3G5QLRgyXOJaqEMWwKoMAMQ3YTjB3EHngZMIVAOhyPNMzFfePqBVv-MfbZFSIUQrwL1OFp-R3YiUxmkcY6qBQ6mZXs8ICmcDpn-7ReTMEbcP8yWWmgKdKRIWW5OIuQej8I07eeo4m17nZGyYSb0mn8tQ0E2p60VG5A"
const jwkUrl = "https://cognito-idp.us-west-1.amazonaws.com/us-west-1_VkAfnzqZF/.well-known/jwks.json"

func TestValidation(t *testing.T) {
	_, err := utils.VerifyJwt(expiredToken, jwkUrl)
	assert.Error(t, err)
}

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
					routingHeader:   "application/vnd.api+json,application/vnd.api+json; route[/api/products*:GET]=lambda[" + os.Getenv("TEST_LAMBDA") + "]",
					"Host":          "localhost",
					"Authorization": accessToken,
				},
				Path:       "/api/products",
				HTTPMethod: "GET",
			},
			expect: "application/vnd.api+json,application/vnd.api+json; route[/api/products*:GET]=lambda[" + os.Getenv("TEST_LAMBDA") + "]",
			err:    nil,
		},
		{
			request: events.APIGatewayProxyRequest{
				Body: "My Request",
				Headers: map[string]string{
					routingHeader:   "application/vnd.api+json,application/vnd.api+json; route[/post*:POST]=url[https://postman-echo.com]",
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
					routingHeader:   "application/vnd.api+json,application/vnd.api+json; route[/post*:GET]=url[https://postman-echo.com]; route[/post*:POST]=path[/post*:GET]",
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
					routingHeader:   "application/vnd.api+json,application/vnd.api+json; route[/put*:PUT]=url[https://postman-echo.com]",
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
					routingHeader:   "application/vnd.api+json,application/vnd.api+json; route[/get*:GET]=url[https://postman-echo.com]",
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
					routingHeader:   "application/vnd.api+json,application/vnd.api+json; route[/common:GET]=url[https://postman-echo.com]; route[/get*:GET]=path[/common:GET]",
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
					routingHeader:   "application/vnd.api+json,application/vnd.api+json; route[/common:POST]=sqs[" + os.Getenv("TEST_SQS") + "]",
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
