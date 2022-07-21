This project is Yeoman as a service. It provides the ability to installed versioned NPM generator packages,
run them with predefined values for options, arguments, and questions, and returns the result as a 
zip file.

This project is paired with a no-code [customizable frontend](/js/template-customizable-frontend) that allows
exposes a simple UI defined in JSON to provide a completely web based workflow for generating and consuming
Yeoman generators.

The frontend and backend can be run locally with [Docker compose](/docker/customizable-workflow-builder).

# Running the service

## Run with Docker

Run the service Docker image with the command:

```
docker run -p 4000:4000 -e UNSAFE_ENABLE_NPM_INSTALL=true octopussamples/workflowbuildertemplategenerator
```

## Generating templates

The following request initiates the generation of a template: 

```
curl 'http://localhost:4000/api/template' -X POST -H 'Content-Type: application/vnd.api+json' --data-raw '{"data":{"type":"generatetemplate","attributes":{"generator":"generator-springboot","options":{},"answers":{"appName":"myservice","packageName":"com.mycompany.myservice","databaseType":"postgresql","dbMigrationTool":"flywaydb","buildTool":"maven"},"args":[]}}}'
```

It returns an ID that is used to download the generated template with a second request:

```
{"data":{"type":"template","id":"ZWNlNWE2YmZkZGFhMTVjOTQ4ZDE2NTQ0YTY5ZjdlMzI="}}
```

The ID is passed to the `/download/template` endpoint. This endpoint returns 404 while the template is being generated, and returns the template zip file when it is availbale:

```
curl 'http://localhost:4000/download/template/ZWNlNWE2YmZkZGFhMTVjOTQ4ZDE2NTQ0YTY5ZjdlMzI='
```

## Request JSON structure

The JSON passed to the `/api/template` endpoint is strucuted as a [JSON API](https://jsonapi.org/) object. It has the following attributes:

| Attribute  | Description  |
|---|---|
| generator  | The full name of the NPM package holding the Yeoman generator. Note this must include the `generator` prefix. It can optionally include a package version (e.g. `generator-springboot@0.0.10`), the subgenerator name (e.g. generator-springboot:app), or a combination of the two (e.g. generator-springboot@0.0.10:app).  |
| options  | An object holding the options to pass to the generator. See the [Yeoman docs](https://yeoman.io/authoring/user-interactions.html) for more info on options, arguments, and questions.  |
| answers  | An object holding the answers to pass to the generator. See the [Yeoman docs](https://yeoman.io/authoring/user-interactions.html) for more info on options, arguments, and questions.  | 
| args  | An object holding the arguments to pass to the generator. See the [Yeoman docs](https://yeoman.io/authoring/user-interactions.html) for more info on options, arguments, and questions.  | 



# Building and debugging

## Install dependencies

By default, dependencies were installed when this application was generated.
Whenever dependencies in `package.json` are changed, run the following command:

```sh
npm install
```

To only install resolved dependencies in `package-lock.json`:

```sh
npm ci
```

## Run the application

```sh
npm start
```

You can also run `node .` to skip the build step.

Open http://127.0.0.1:4000 in your browser.

## Rebuild the project

To incrementally build the project:

```sh
npm run build
```

To force a full build by cleaning up cached artifacts:

```sh
npm run rebuild
```

## Fix code style and formatting issues

```sh
npm run lint
```

To automatically fix such issues:

```sh
npm run lint:fix
```

## Other useful commands

- `npm run migrate`: Migrate database schemas for models
- `npm run openapi-spec`: Generate OpenAPI spec into a file
- `npm run docker:build`: Build a Docker image for this application
- `npm run docker:run`: Run this application inside a Docker container

## Tests

```sh
npm test
```
