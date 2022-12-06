 # Content Team Monorepo

This repo contains the microservices supporting the content team.

And architecture diagram can be found [here](https://app.cloudcraft.co/view/089e13fd-5130-4806-a235-668c53c8ca2f?key=4f239d74-6783-401b-96cd-db0ee17fcf6d).

## Octopus Workflow Builder

Visit the [wiki](https://github.com/OctopusSamples/content-team-apps/wiki/Octopus-Workflow-Builder) for a detailed explaination of the workflow builder.

## Badges

### Audits Service
![Branches](.github/badges/auditsbranches.svg)
![Coverage](.github/badges/audits.svg)

### GitHub Actions Workflow Generator
![Branches](.github/badges/githubbranches.svg)
![Coverage](.github/badges/github.svg)

### Jenkins Pipelines Generator
![Branches](.github/badges/jenkinsbranches.svg)
![Coverage](.github/badges/jenkins.svg)

### Azure Service Bus Proxy
![Branches](.github/badges/azure-servicebus-proxy-branches.svg)
![Coverage](.github/badges/azure-servicebus-proxy-coverage.svg)

### GitHub OAuth Proxy
![Branches](.github/badges/github-oauth-proxy-branches.svg)
![Coverage](.github/badges/github-oauth-proxy-coverage.svg)

### Microservice Utils Shared Library
![Branches](.github/badges/microservice-utils-branches.svg)
![Coverage](.github/badges/microservice-utils-coverage.svg)

### Repo Creator
![Branches](.github/badges/repocreator.svg)
![Coverage](.github/badges/repocreatorbranches.svg)

### GitHub Repo Proxy
![Branches](.github/badges/githubrepoproxy.svg )
![Coverage](.github/badges/githubrepoproxybranches.svg)

### Octopus Proxy
![Branches](.github/badges/githubrepoproxy.svg )
![Coverage](.github/badges/octopusproxybranches.svg)

### Reverse Proxy
[![Go Report Card](https://goreportcard.com/badge/github.com/OctopusSamples/content-team-apps/go/reverse-proxy)](https://goreportcard.com/report/github.com/OctopusSamples/content-team-apps/go/reverse-proxy)

## Links

* [Github Actions Workflow Generator](https://githubactionsworkflowgenerator.octopus.com/#/)
* [Jenkins Pipeline Generator](https://jenkinspipelinegenerator.octopus.com/#/)

## Using Sample Apps

This repo produces sample web apps that are designed to easily demonstrate Octopus features like:

* Tenant deployments
* Rollbacks
* Config file modification
* Feature branches
* Kubernetes, ECS deployments
* AWS Lambda deployments
* Serverless.io deployments
* Microservices
* Testable deployments (i.e. health checks)

The sections below document how to run the sample apps on various platforms.

### Docker

The following instructions are used to run the sample apps locally:

1. `cd docker\octopub`
2. `docker compose up`

After a minute or so open the frontend at http://localhost:5000.

### AWS Lambda

A deployed example of this application can be found [here](https://octopub-frontend.s3.amazonaws.com/index.html).

Zip artifacts have been uploaded to a Maven feed hosted by GitHub packages.

Unfortunately you [can not download packages without authentication](https://github.com/orgs/community/discussions/26634). So you need to generate a GitHub Personal Access Token that has the package read permission.

Download the latest version of the frontend web app with:

```bash
mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:get \
  "-DrepoUrl=https://github_user:personal_access_token@maven.pkg.github.com/OctopusSamples/content-team-apps" \
  "-Dartifact=com.octopus:frontend-webapp-serverless:LATEST:zip" \
  "-Ddest=frontend-webapp-serverless.zip"
```

Download the latest version of the product microservice with:

```bash
mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:get \
  "-DrepoUrl=https://github_user:personal_access_token@maven.pkg.github.com/OctopusSamples/content-team-apps" \
  "-Dartifact=com.octopus:products-service-lambda:LATEST:zip" \
  "-Ddest=products-service-lambda.zip"
```

Unzip the packages:

```bash
mkdir frontend-webapp-serverless
mkdir products-service-lambda
unzip frontend-webapp-serverless.zip -d frontend-webapp-serverless
unzip products-service-lambda.zip -d products-service-lambda
```

Deploy the apps with serverless.io, replacing `tenantname` with a short, unique tenant name:

```bash
pushd products-service-lambda
serverless deploy --param="tenantName=tenantname"
popd

pushd frontend-webapp-serverless
serverless plugin install -n serverless-s3-sync
serverless plugin install -n serverless-plugin-dot-template
serverless deploy --param="tenantName=tenantname"
popd
```

Retrieve the hostname of the S3 bucket holding the frontend web application, replacing `tenantname` with the parameter passed into the `serverless deploy` commands above:

```
aws cloudformation describe-stacks \
    --stack-name <tenantname>octopub-frontend-dev \
    --query "Stacks[0].Outputs[?OutputKey=='StaticSiteDomain'].OutputValue" \
    --output text
```

Then open https://domain/index.html to view the web app.

## Azure Functions

### Backend

Download the latest version of the product microservice with:

```bash
mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:get \
  "-DrepoUrl=https://github_user:personal_access_token@maven.pkg.github.com/OctopusSamples/content-team-apps" \
  "-Dartifact=com.octopus:products-service-azurefunc:LATEST:zip" \
  "-Ddest=product-service-azure.zip"
```

Create a resource group, storage account, and function. Then upload the ZIP file, create an SAS token, and set the `WEBSITE_RUN_FROM_PACKAGE` setting to the SAS URL. Ensure you change the `RESOURCE_GROUP`, `FUNCTION_NAME`, and `STORAGE_ACCOUNT` names to be unique.

See [here](https://stackoverflow.com/questions/74695741/how-can-i-deploy-a-prepackaged-quarkus-azure-functions-app/74695742#74695742) for more details on this process:

```bash
REGION=australiaeast
# Change this to be unique
RESOURCE_GROUP=octopubproductservice
# Change this to be unique
FUNCTION_NAME=octopubproductservice
# Change this to be unique
STORAGE_ACCOUNT=octopubproductservice
STORAGE_SKU="Standard_LRS"
ZIP_FILE=product-service-azure.zip
CURRENT_DATE=$(date +%Y%m%d)
SAS_EXPIRY=$(date -d "$CURRENT_DATE +10 years" +%Y-%m-%d)

# Create a resource group
az group create --location $REGION --name $RESOURCE_GROUP
# Create a storage account
az storage account create --name $STORAGE_ACCOUNT --resource-group $RESOURCE_GROUP --sku $STORAGE_SKU
# Create a function app
az functionapp create \
  --name $FUNCTION_NAME \
  --resource-group $RESOURCE_GROUP \
  --storage-account $STORAGE_ACCOUNT \
  --consumption-plan-location $REGION \
  --functions-version 4 \
  --os-type linux \
  --runtime java \
  --runtime-version 11.0
# Create the container
az storage container create \
  --name java-functions-run-from-packages \
  --account-name $STORAGE_ACCOUNT
# Upload the function package
az storage blob upload \
  --account-name $STORAGE_ACCOUNT \
  --container-name java-functions-run-from-packages \
  --name product-service-azure.zip \
  --file $ZIP_FILE \
  --overwrite \
  --auth-mode key
# Create a SAS key for the function package
URL=$(az storage blob generate-sas \
  --account-name $STORAGE_ACCOUNT \
  --container-name java-functions-run-from-packages \
  --name $ZIP_FILE \
  --permissions r \
  --expiry $SAS_EXPIRY \
  --auth-mode key \
  --full-uri)
# The URL is quoted. We treat this as a JSON string, and use jq to return the raw string
FIXED_URL=$(echo $URL | jq -r '.')
# The raw string is set as the WEBSITE_RUN_FROM_PACKAGE value, which indicates Azure
# must download the function from the URL.
az functionapp config appsettings set \
  --name $FUNCTION_NAME \
  --resource-group $RESOURCE_GROUP \
  --settings "WEBSITE_RUN_FROM_PACKAGE=$FIXED_URL"
# Enable CORS
az functionapp cors add \
  -g $RESOURCE_GROUP \
  -n $FUNCTION_NAME \
  --allowed-origins "*"
```

### Frontend

Download the latest version of the frontend web app with:

```bash
mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:get \
  "-DrepoUrl=https://github_user:personal_access_token@maven.pkg.github.com/OctopusSamples/content-team-apps" \
  "-Dartifact=com.octopus:frontend-webapp-serverless:LATEST:zip" \
  "-Ddest=frontend-webapp-serverless.zip"
```

Unzip the packages:

```bash
mkdir frontend-webapp-serverless
unzip frontend-webapp-serverless.zip -d frontend-webapp-serverless
```

Edit the config file to point to the backend Azure function:

```bash
cat <<< $(jq '.productEndpoint = "https://octopubproductservice.azurewebsites.net/api/products"' build/config.json) > build/config.json
cat <<< $(jq '.productHealthEndpoint = "https://octopubproductservice.azurewebsites.net/health/products"' build/config.json) > build/config.json
```

[Upload the static web app to the storage account](https://learn.microsoft.com/en-us/azure/storage/blobs/storage-blob-static-website-how-to?tabs=azure-cli):

```bash
# Change this to be unique
RESOURCE_GROUP=octopubproductservice
# Change this to be unique
STORAGE_ACCOUNT=octopubproductservice

az storage blob service-properties update \
  --account-name $STORAGE_ACCOUNT \
  --static-website \
  --404-document error.html \
  --index-document index.html
az storage blob upload-batch \
  -s build \
  -d '$web' \
  --account-name $STORAGE_ACCOUNT \
  --overwrite
az storage account show \
  -n $STORAGE_ACCOUNT \
  -g $RESOURCE_GROUP \
  --query "primaryEndpoints.web" \
  --output tsv
```