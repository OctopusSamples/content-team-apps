resource "octopusdeploy_library_variable_set" "frontend_library_variable_set" {
  name = "App Builder Frontend"
  description = "Variables used when deploying the App Builder Frontend"
}

output "frontend_library_variable_set_id" {
  value = octopusdeploy_library_variable_set.frontend_library_variable_set.id
}

resource "octopusdeploy_variable" "aws_account" {
  name = "AWS.Account"
  type = "AmazonWebServicesAccount"
  description = "The AWS account used to deploy the application."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = var.octopus_development_aws_account_id
}

resource "octopusdeploy_variable" "aws_region" {
  name = "AWS.Region"
  type = "String"
  description = "The AWS region."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = var.aws_region
}

resource "octopusdeploy_variable" "aws_s3_bucket" {
  name = "CloudFormation.S3Bucket"
  type = "String"
  description = "The name of the bucket hosting the App Builder frontend web app."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "AppBuilder.Frontend"
}

resource "octopusdeploy_variable" "aws_s3_bucket" {
  name = "S3.Directory"
  type = "String"
  description = "The S3 'directory' that holds the frontend web app files for a given deployment. This directory is based on the package ID."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "#{Octopus.Action[Upload Frontend].Package[].PackageId}.#{Octopus.Action[Upload Frontend].Package[].PackageVersion}"
}

resource "octopusdeploy_variable" "webapp_hostname" {
  name = "WebApp.Hostname"
  type = "String"
  description = "The hostname that users are redirected to if they access a missing page."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "https://example.org"
}

resource "octopusdeploy_variable" "cloudformation_apigateway" {
  name = "CloudFormationName.ApiGateway"
  type = "String"
  description = "The Cloudformation stack that created the app builder API gateway."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "AppBuilderApiGateway"
}