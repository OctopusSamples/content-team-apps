// Package lambdahandler contains the AWS lambda entry point
package lambdahandler

import (
	"context"
	"encoding/base64"
	"encoding/json"
	"errors"
	"github.com/OctopusSamples/OctoPub/go/reverse-proxy/internal/pkg/utils"
	"github.com/aws/aws-lambda-go/events"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/lambda"
	"github.com/aws/aws-sdk-go/service/sqs"
	"github.com/awslabs/aws-lambda-go-api-proxy/handlerfunc"
	"github.com/dgrijalva/jwt-go"
	"github.com/vibrantbyte/go-antpath/antpath"
	"log"
	"net/http"
	"net/http/httputil"
	"net/url"
	"os"
	"regexp"
	"strconv"
	"strings"
)

var matcher = antpath.New()
var groupPath = regexp.MustCompile(`/api/(?:[a-zA-Z]+)/?`)

// HandleRequest takes the incoming Lambda request and forwards it to the downstream service
// defined in the "Accept" headers.
func HandleRequest(_ context.Context, req events.APIGatewayProxyRequest) (events.APIGatewayProxyResponse, error) {
	resp, err := processRequest(req)
	if err != nil {
		log.Println("ReverseProxy-Handler-GeneralFailure " + err.Error())
		return events.APIGatewayProxyResponse{}, err
	}
	return resp, nil
}

func processRequest(req events.APIGatewayProxyRequest) (events.APIGatewayProxyResponse, error) {
	upstreamUrl, upstreamLambda, upstreamSqs, err := extractUpstreamService(req)

	if err == nil {

		if upstreamUrl != nil {
			return httpReverseProxy(upstreamUrl, req)
		}

		if upstreamLambda != "" {
			return callLambda(upstreamLambda, req)
		}

		if upstreamSqs != "" {
			return callSqs(upstreamSqs, req)
		}
	}

	if os.Getenv("DEFAULT_LAMBDA") != "" {
		return callLambda(os.Getenv("DEFAULT_LAMBDA"), req)
	} else if os.Getenv("DEFAULT_SQS") != "" {
		return callSqs(os.Getenv("DEFAULT_SQS"), req)
	} else {
		url, err := url.Parse(os.Getenv("DEFAULT_URL"))
		if err != nil {
			log.Println("ReverseProxy-Handler-UrlParseError " + err.Error())
			return events.APIGatewayProxyResponse{}, err
		}
		return httpReverseProxy(url, req)
	}
}

func httpReverseProxy(upstreamUrl *url.URL, req events.APIGatewayProxyRequest) (events.APIGatewayProxyResponse, error) {
	log.Println("lambdahandler.httpReverseProxy(*url.URL, events.APIGatewayProxyRequest)")
	log.Println("Calling URL " + upstreamUrl.String())

	handler := func(w http.ResponseWriter, httpReq *http.Request) {
		// The host header for the upstream requests must match the upstream server
		// https://github.com/golang/go/issues/28168
		httpReq.Host = upstreamUrl.Host
		proxy := httputil.NewSingleHostReverseProxy(upstreamUrl)
		proxy.ServeHTTP(w, httpReq)
	}

	adapter := handlerfunc.New(handler)
	resp, proxyErr := adapter.ProxyWithContext(context.Background(), req)

	if proxyErr != nil {
		log.Println("ReverseProxy-Url-GeneralFailure " + proxyErr.Error())
		return events.APIGatewayProxyResponse{}, proxyErr
	}

	return resp, nil
}

func callSqs(queueURL string, req events.APIGatewayProxyRequest) (events.APIGatewayProxyResponse, error) {
	log.Println("lambdahandler.callSqs(string, events.APIGatewayProxyRequest)")
	log.Println("Calling SQS " + queueURL)

	sess := session.Must(session.NewSession())

	svc := sqs.New(sess)

	acceptHeader, err := getHeader(req.Headers, req.MultiValueHeaders, "Accept")

	if err != nil {
		acceptHeader = ""
	}

	body := req.Body

	if req.IsBase64Encoded {
		decodedBody, decodeError := base64.StdEncoding.DecodeString(body)
		if decodeError == nil {
			body = string(decodedBody)
		} else {
			log.Println("ReverseProxy-SQS-BodyDecodeError " + decodeError.Error())
			return events.APIGatewayProxyResponse{}, decodeError
		}
	}

	_, sqsErr := svc.SendMessage(&sqs.SendMessageInput{
		DelaySeconds: aws.Int64(0),
		MessageAttributes: map[string]*sqs.MessageAttributeValue{
			"action": &sqs.MessageAttributeValue{
				DataType:    aws.String("String"),
				StringValue: aws.String(getAction(req)),
			},
			"entity": &sqs.MessageAttributeValue{
				DataType:    aws.String("String"),
				StringValue: aws.String(utils.GetEnv("ENTITY_TYPE", "Unknown")),
			},
			"dataPartition": &sqs.MessageAttributeValue{
				DataType:    aws.String("String"),
				StringValue: aws.String(acceptHeader),
			},
		},
		MessageBody: aws.String(body),
		QueueUrl:    &queueURL,
	})

	if sqsErr != nil {
		log.Println("ReverseProxy-SQS-GeneralFailure " + sqsErr.Error())
		return events.APIGatewayProxyResponse{}, sqsErr
	}

	return events.APIGatewayProxyResponse{StatusCode: 201}, nil
}

func callLambda(lambdaName string, req events.APIGatewayProxyRequest) (events.APIGatewayProxyResponse, error) {
	log.Println("lambdahandler.callLambda(string, events.APIGatewayProxyRequest)")
	log.Println("Calling Lambda " + lambdaName)

	sess := session.Must(session.NewSession())

	region := utils.GetEnv("AWS_REGION", "us-west-1")
	client := lambda.New(sess, &aws.Config{Region: &region})
	payload, err := json.Marshal(req)

	if err != nil {
		return events.APIGatewayProxyResponse{}, err
	}

	lambdaResponse, lambdaErr := client.Invoke(&lambda.InvokeInput{FunctionName: aws.String(lambdaName), Payload: payload})

	if lambdaErr != nil {
		log.Println("ReverseProxy-Lambda-GeneralFailure " + lambdaErr.Error())
		return events.APIGatewayProxyResponse{}, lambdaErr
	}

	return convertLambdaProxyResponse(lambdaResponse)
}

func authorizeRouting(req events.APIGatewayProxyRequest) bool {
	// The requirements for a cognito login can be disabled
	required := utils.GetEnv("COGNITO_AUTHORIZATION_REQUIRED", "true")
	if strings.ToLower(required) == "false" {
		return true
	}

	authentication, authErr := getHeader(req.Headers, req.MultiValueHeaders, "Authorization")
	if authErr != nil {
		log.Println("ReverseProxy-Jwt-InvalidHeader No authorization header found")
		return false
	}

	// We expect a header like "Bearer: tokengoeshere"
	splitHeader := strings.Split(authentication, ":")
	if len(splitHeader) != 2 {
		log.Println("ReverseProxy-Jwt-InvalidHeader No bearer token found in authorization header")
		return false
	}

	if strings.TrimSpace(splitHeader[0]) != "Bearer" {
		log.Println("ReverseProxy-Jwt-InvalidHeader No bearer token found in authorization header")
		return false
	}

	region, regionOk := os.LookupEnv("COGNITO_REGION")
	pool, poolOk := os.LookupEnv("COGNITO_POOL")
	requiredGroup, requiredGroupOk := os.LookupEnv("COGNITO_REQUIRED_GROUP")

	if !regionOk || !poolOk || !requiredGroupOk {
		log.Println("ReverseProxy-Jwt-InvalidConfig The environment variables COGNITO_REGION, COGNITO_POOL, and COGNITO_REQUIRED_GROUP must be defined")
		return false
	}

	token, tokenError := utils.VerifyJwt(
		strings.TrimSpace(splitHeader[1]),
		"https://cognito-idp."+region+".amazonaws.com/"+pool+"/.well-known/jwks.json")

	if tokenError != nil || !token.Valid {
		log.Println("ReverseProxy-Jwt-ValidationError " + tokenError.Error())
		return false
	}

	if groups, ok := token.Claims.(jwt.MapClaims)["cognito:groups"]; ok {
		for _, group := range groups.([]interface{}) {
			if group == requiredGroup {
				return true
			}
		}
	}

	log.Println("ReverseProxy-Jwt-MissingGroup The JWT did not include the group " + requiredGroup +
		". Routing will not be performed.")
	return false
}

func extractUpstreamService(req events.APIGatewayProxyRequest) (http *url.URL, lambda string, sqs string, err error) {
	acceptAll, err := getHeader(req.Headers, req.MultiValueHeaders, "Accept")

	if err != nil {
		return nil, "", "", errors.New("accept header is required")
	}

	if !authorizeRouting(req) {
		return nil, "", "", errors.New("user is not authorized to route requests")
	}

	for _, acceptComponent := range getComponentsFromHeader(acceptAll) {
		path, method, destination, err := getRuleComponents(acceptComponent)
		if err == nil {
			if pathAndMethodIsMatch(path, method, req) {

				// for convenience, rules can reference the destinations of other paths, allowing
				// complex rule sets to be updated with a single destination
				pathDest, err := getDestinationPath(acceptAll, destination)

				if err == nil {
					destination = pathDest
				}

				url, err := getDestinationUrl(destination)

				if err == nil {
					return url, "", "", nil
				}

				lambda, err := getDestinationLambda(destination)

				if err == nil {
					return nil, lambda, "", nil
				}

				sqs, err := getDestinationSqs(destination)

				if err == nil {
					return nil, "", sqs, nil
				}
			}
		}
	}

	return nil, "", "", errors.New("failed to find downstream service")
}

func getComponentsFromHeader(header string) []string {
	var returnArray []string
	headerArray := strings.Split(header, ",")
	for _, element := range headerArray {
		components := strings.Split(element, ";")
		returnArray = append(returnArray, components...)
	}

	return returnArray
}

func pathAndMethodIsMatch(path string, method string, req events.APIGatewayProxyRequest) bool {
	// The path is an ant matcher that must match the requested path
	pathIsMatch := matcher.Match(path, req.Path)
	// AThe http method must match the current request
	methodIsMatch := strings.EqualFold(method, req.HTTPMethod)

	return pathIsMatch && methodIsMatch
}

func getRuleComponents(acceptComponent string) (string, string, string, error) {
	ruleComponents := strings.Split(strings.TrimSpace(acceptComponent), "=")
	// ensure the component has an equals sign
	if len(ruleComponents) == 2 {
		if strings.HasPrefix(ruleComponents[0], "version[") && strings.HasSuffix(ruleComponents[0], "]") {
			strippedVersion := strings.TrimSuffix(strings.TrimPrefix(ruleComponents[0], "version["), "]")
			pathAndMethod := strings.Split(strippedVersion, ":")
			return pathAndMethod[0], pathAndMethod[1], ruleComponents[1], nil
		}
	}

	return "", "", "", errors.New("component was not a valid rule")
}

func getDestinationPath(acceptAll string, ruleDestination string) (string, error) {
	if strings.HasPrefix(ruleDestination, "path[") && strings.HasSuffix(ruleDestination, "]") {

		strippedDest := strings.TrimSuffix(strings.TrimPrefix(ruleDestination, "path["), "]")

		for _, acceptComponent := range getComponentsFromHeader(acceptAll) {
			path, method, destination, err := getRuleComponents(acceptComponent)
			if err == nil && path+":"+method == strippedDest {
				return destination, nil
			}
		}
	}

	return "", errors.New("destination was not a path, or did not find the path")
}

func getDestinationUrl(ruleDestination string) (*url.URL, error) {
	if strings.HasPrefix(ruleDestination, "url[") && strings.HasSuffix(ruleDestination, "]") {

		trimmedDestination := strings.TrimSpace(strings.TrimSuffix(strings.TrimPrefix(ruleDestination, "url["), "]"))
		if trimmedDestination == "" {
			return nil, errors.New("destination can not be blank")
		}

		// See if the downstream service is a valid URL
		parsedUrl, err := url.Parse(trimmedDestination)

		// downstream service was not a url, so assume it is a lambda
		if err == nil {
			if strings.HasPrefix(trimmedDestination, "http") {
				return parsedUrl, nil
			}
		} else {
			log.Println("ReverseProxy-Url-UrlParseError " + err.Error())
		}
	}

	return nil, errors.New("destination was not a URL")
}

func getDestinationLambda(ruleDestination string) (string, error) {
	if strings.HasPrefix(ruleDestination, "lambda[") && strings.HasSuffix(ruleDestination, "]") {

		destination := strings.TrimSpace(strings.TrimSuffix(strings.TrimPrefix(ruleDestination, "lambda["), "]"))
		if destination == "" {
			return "", errors.New("destination can not be blank")
		}
		return destination, nil
	}

	return "", errors.New("destination was not a lambda")
}

func getDestinationSqs(ruleDestination string) (string, error) {
	if strings.HasPrefix(ruleDestination, "sqs[") && strings.HasSuffix(ruleDestination, "]") {

		destination := strings.TrimSpace(strings.TrimSuffix(strings.TrimPrefix(ruleDestination, "sqs["), "]"))
		if destination == "" {
			return "", errors.New("destination can not be blank")
		}
		return destination, nil
	}

	return "", errors.New("destination was not a sqs")
}

func getHeader(singleHeaders map[string]string, multiHeaders map[string][]string, header string) (string, error) {
	for key, element := range singleHeaders {
		if strings.EqualFold(key, header) {
			return element, nil
		}
	}

	for key, element := range multiHeaders {
		if strings.EqualFold(key, header) {
			return strings.Join(element, ","), nil
		}
	}

	return "", errors.New("key was not found")
}

func getAction(req events.APIGatewayProxyRequest) string {
	method := strings.ToLower(req.HTTPMethod)

	if method == "get" && groupPath.Match([]byte(method)) {
		return "ReadAll"
	}

	switch method {
	case "get":
		return "Read"
	case "post":
		return "Create"
	case "patch":
		return "Update"
	case "delete":
		return "Delete"
	}
	return "None"
}

func convertLambdaProxyResponse(lambdaResponse *lambda.InvokeOutput) (events.APIGatewayProxyResponse, error) {
	var data LenientAPIGatewayProxyResponse
	jsonErr := json.Unmarshal(lambdaResponse.Payload, &data)

	if jsonErr != nil {
		var data2 events.APIGatewayProxyResponse
		jsonErr2 := json.Unmarshal(lambdaResponse.Payload, &data2)

		if jsonErr2 != nil {
			return events.APIGatewayProxyResponse{}, jsonErr2
		}

		return data2, nil
	}

	apiGatewayProxyResponse, conErr := data.toAPIGatewayProxyResponse()

	if conErr != nil {
		return events.APIGatewayProxyResponse{}, conErr
	}

	return apiGatewayProxyResponse, nil
}

type LenientAPIGatewayProxyResponse struct {
	StatusCode        string              `json:"statusCode"`
	Headers           map[string]string   `json:"headers"`
	MultiValueHeaders map[string][]string `json:"multiValueHeaders"`
	Body              string              `json:"body"`
	IsBase64Encoded   bool                `json:"isBase64Encoded,omitempty"`
}

func (r *LenientAPIGatewayProxyResponse) toAPIGatewayProxyResponse() (events.APIGatewayProxyResponse, error) {
	statusCode, err := strconv.Atoi(r.StatusCode)

	if err != nil {
		return events.APIGatewayProxyResponse{}, err
	}

	return events.APIGatewayProxyResponse{
		StatusCode:        statusCode,
		Headers:           r.Headers,
		MultiValueHeaders: r.MultiValueHeaders,
		Body:              r.Body,
		IsBase64Encoded:   r.IsBase64Encoded,
	}, nil
}
