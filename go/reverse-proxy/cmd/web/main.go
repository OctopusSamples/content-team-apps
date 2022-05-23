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
	healthPort := utils.GetEnv("HEALTH_PORT", "8081")

	proxyHandler := http.NewServeMux()
	proxyHandler.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		buf := new(bytes.Buffer)
		buf.ReadFrom(r.Body)
		body := buf.String()

		/*
			Need to add the host header back into a collection, because it is removed from the
			Header collection. This is from the docs:
			For incoming requests, the Host header is promoted to the Request.Host field and removed from the Header map.
		*/
		headers := r.Header
		headers.Set("Host", r.Host)

		request := events.APIGatewayProxyRequest{
			MultiValueHeaders:               headers,
			Path:                            r.URL.Path,
			Body:                            body,
			HTTPMethod:                      r.Method,
			IsBase64Encoded:                 false,
			MultiValueQueryStringParameters: r.URL.Query(),
		}
		response, _ := lambdahandler.HandleRequest(context.TODO(), request)

		for key, value := range response.Headers {
			w.Header().Set(key, value)
		}

		for multiValueKey, multiValue := range response.MultiValueHeaders {
			for _, value := range multiValue {
				w.Header().Add(multiValueKey, value)
			}
		}

		w.WriteHeader(response.StatusCode)

		w.Write([]byte(response.Body))
	})

	healthHandler := http.NewServeMux()
	healthHandler.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		w.Write([]byte("OK"))
	})

	log.Println("Listening on port " + port)
	log.Println("Listening on health check port " + healthPort)

	// Listen for health checks for the proxy
	go func() {
		http.ListenAndServe(":"+healthPort, healthHandler)
	}()

	// Proxy the requests
	log.Fatal(http.ListenAndServe(":"+port, proxyHandler))

}
