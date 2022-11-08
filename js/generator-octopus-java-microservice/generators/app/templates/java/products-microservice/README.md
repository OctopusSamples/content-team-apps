The products microservice is written in Quarkus. It can either produce a package for deployment to
AWS Lambda, or produce a self-contained web application.

# Building a Lambda

The Lambda requires an external MySQL compatible database. The database is configured with the 
following environment variables:

* DATABASE_HOSTNAME
* DATABASE_PORT
* DATABASE_NAME
* DATABASE_USERNAME
* DATABASE_PASSWORD

```bash
# Install the shared libraries
./mvn clean install
# Enter the products microservice directory
cd products-microservice
# Build the native lambda package
./mvnw clean package -P "native,lambda" -Dquarkus.profile=faas
```

The Lambda exposes two handlers.

The default handler exposes the REST API. This handler is optionally specified by setting the
`LAMBDA_NAME` environment variable to `ResourceHandler`.

The second resource handler runs a database upgrade. This handler is specified by setting the
`LAMBDA_NAME` environment variable to `DatabaseInit`.

Typically, a deployment process will deploy the Lambda twice, one with `LAMBDA_NAME` set to `ResourceHandler`,
and one with `LAMBDA_NAME` set to `DatabaseInit`. 

The `DatabaseInit` Lambda is then executed. This prepares the database.

Then traffic is sent to the `ResourceHandler` Lambda.

# Building a web application

The web application is most easily used with its in memory H2 database. The H2 database does not
support native compilation, so the web app is built to a jar file.

```bash
# Install the shared libraries
./mvn clean install
# Enter the products microservice directory
cd products-microservice
# Build the native lambda package
./mvnw clean package
```

You can alternatively build a native executable that accesses an external database. The database 
is configured with the following environment variables:

* DATABASE_HOSTNAME
* DATABASE_PORT
* DATABASE_NAME
* DATABASE_USERNAME
* DATABASE_PASSWORD

```bash
# Build the native lambda package
./mvnw clean package -P "native" -Dquarkus.profile=mysql
```