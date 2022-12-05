#!/bin/bash

# https://learn.microsoft.com/en-us/azure/azure-functions/scripts/functions-cli-create-serverless
# This script creates a new Azure function and deploys the products service.

# You need to run the following first:
# az login
# az account set --subscription "Team Sales Engineering - Sandbox"

REGION=australiaeast
RESOURCE_GROUP=octopubproductservice
FUNCTION_NAME=octopubproductservice
STORAGE_ACCOUNT=octopubproductservice
STORAGE_SKU="Standard_LRS"

az group create --location $REGION --name $RESOURCE_GROUP
az storage account create --name $STORAGE_ACCOUNT --resource-group $RESOURCE_GROUP --sku $STORAGE_SKU
az functionapp create \
  --name $FUNCTION_NAME \
  --resource-group $RESOURCE_GROUP \
  --storage-account $STORAGE_ACCOUNT \
  --consumption-plan-location $REGION \
  --functions-version 4 \
  --os-type linux \
  --runtime java \
  --runtime-version 11.0
func azure functionapp publish $FUNCTION_NAME --java