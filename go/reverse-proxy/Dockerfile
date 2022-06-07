# build stage
FROM golang:alpine AS build-env
ADD . /src
RUN cd /src && go build cmd/web/main.go

# final stage
FROM alpine
WORKDIR /app
COPY --from=build-env /src/main /app/
ENTRYPOINT ./main