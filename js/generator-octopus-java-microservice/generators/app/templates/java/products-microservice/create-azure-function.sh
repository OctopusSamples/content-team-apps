#!/bin/bash

# The process of deploying a compiled function package is surprisingly difficult and undocumented,
# especially for Java. The official way to do this is with the maven azure-functions-maven-plugin
# plugin, but this requires that you rebuild your code each time, which we consider to be an
# anti-pattern.
#
# This script creates the resource group, storage account, and azure function. It then
# uploads the Java package, creates a long lived SAS token, and defines the
# WEBSITE_RUN_FROM_PACKAGE setting to the SAS url.
#
# This emulates the process implemented by the RunFromBlobFunctionDeployHandler
# (https://github.com/microsoft/azure-maven-plugins/blob/develop/azure-toolkit-libs/azure-toolkit-appservice-lib/src/main/java/com/microsoft/azure/toolkit/lib/appservice/deploy/RunFromBlobFunctionDeployHandler.java)
# class used by the maven plugin.

# You need to run the following first, where the subscription ID is for "Team Sales Engineering - Sandbox":
# az login
# az account set --subscription "3b50dcf4-f74d-442e-93cb-301b13e1e2d5"

REGION=australiaeast
RESOURCE_GROUP=octopubproductservice
FUNCTION_NAME=octopubproductservice
STORAGE_ACCOUNT=octopubproductservice
STORAGE_SKU="Standard_LRS"
ZIP_FILE=product-service-azure.zip
CURRENT_DATE=$(date +%Y%m%d)
SAS_EXPIRY=$(date -d "$CURRENT_DATE +10 years" +%Y-%m-%d)

# Recreate the folder structure documented at
# https://learn.microsoft.com/en-us/azure/azure-functions/functions-reference-java?tabs=bash%2Cconsumption#folder-structure
rm -rf /tmp/octopubproductservice
mkdir /tmp/octopubproductservice
mkdir /tmp/octopubproductservice/octopubproductservice
cp target/products-microservice-runner.jar /tmp/octopubproductservice
cp azure-config/host.json /tmp/octopubproductservice
cp azure-config/function.json /tmp/octopubproductservice/octopubproductservice
pushd /tmp/octopubproductservice
zip -r $ZIP_FILE .
popd

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
  --file /tmp/octopubproductservice/$ZIP_FILE \
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