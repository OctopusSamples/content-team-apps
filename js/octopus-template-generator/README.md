This project is Yeoman as a service. It provides the ability to installed versioned NPM generator packages,
run them with predefined values for options, arguments, and questions, and returns the result as a 
zip file.

This project is paired with a no-code [customizable frontend](/js/template-customizable-frontend) that allows
exposes a simple UI defined in JSON to provide a completely web based workflow for generating and consuming
Yeoman generators.

The frontend and backend can be run locally with [Docker compose](/docker/customizable-workflow-builder).

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

Open http://127.0.0.1:3000 in your browser.

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