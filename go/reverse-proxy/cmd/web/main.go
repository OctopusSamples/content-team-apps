package main

import (
	"bytes"
	"context"
	"github.com/OctopusSamples/content-team-apps/go/reverse-proxy/internal/pkg/lambdahandler"
	"github.com/OctopusSamples/content-team-apps/go/reverse-proxy/internal/pkg/utils"
	"github.com/aws/aws-lambda-go/events"
	"log"
	"net/http"
)

func main() {

	port := utils.GetEnv("PORT", "8080")

	http.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		buf := new(bytes.Buffer)
		buf.ReadFrom(r.Body)
		newStr := buf.String()

		request := events.APIGatewayProxyRequest{
			MultiValueHeaders:               r.Header,
			Path:                            r.RequestURI,
			Body:                            newStr,
			HTTPMethod:                      r.Method,
			IsBase64Encoded:                 false,
			MultiValueQueryStringParameters: r.URL.Query(),
		}
		response, _ := lambdahandler.HandleRequest(context.TODO(), request)

		w.WriteHeader(response.StatusCode)

		for s, s2 := range response.Headers {
			w.Header().Add(s, s2)
		}

		w.Write([]byte(response.Body))
	})

	log.Println("Listening on port " + port)

	log.Fatal(http.ListenAndServe(":"+port, nil))

}
