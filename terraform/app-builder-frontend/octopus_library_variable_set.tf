resource "octopusdeploy_library_variable_set" "frontend_library_variable_set" {
  name = "App Builder Frontend"
  description = "Variables used when deploying the App Builder Frontend"
}

output "frontend_library_variable_set_id" {
  value = octopusdeploy_library_variable_set.frontend_library_variable_set.id
}

resource "octopusdeploy_variable" "aws_development_account" {
  name = "AWS.Account"
  type = "AmazonWebServicesAccount"
  description = "The AWS account used to deploy the application. Don't edit these variables directly - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = var.octopus_development_aws_account_id
  scope {
    environments = [var.octopus_development_environment_id, var.octopus_development_security_environment_id]
  }
}

resource "octopusdeploy_variable" "aws_production_account" {
  name = "AWS.Account"
  type = "AmazonWebServicesAccount"
  description = "The AWS account used to deploy the application."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = var.octopus_production_aws_account_id
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
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
  description = "The name of the stack creating the App Builder frontend S3 bucket."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "AppBuilderFrontend"
}

resource "octopusdeploy_variable" "aws_cloudformation_frontend" {
  name = "CloudFormation.Frontend"
  type = "String"
  description = "The name of the stack hosting the frontend API Gateway resources."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "AppBuilderFrontendApiGateway"
}

resource "octopusdeploy_variable" "aws_s3_directory" {
  name = "S3.Directory"
  type = "String"
  description = "The S3 'directory' that holds the frontend web app files for a given deployment. This directory is based on the package version."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "#{Octopus.Action[Upload Frontend].Package[].PackageId}.#{Octopus.Action[Upload Frontend].Package[].PackageVersion}"
}

resource "octopusdeploy_variable" "webapp_subpath" {
  name = "WebApp.SubPath"
  type = "String"
  description = "The directory holding frontend feature branches."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "#{Octopus.Action[Upload Frontend].Package[].PackageVersion | VersionPreRelease}"
}

resource "octopusdeploy_variable" "webapp_hostname" {
  name = "WebApp.Hostname"
  type = "String"
  description = "The hostname that users are redirected to if they access a missing page."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "example.org"
}

resource "octopusdeploy_variable" "cloudformation_apigateway" {
  name = "CloudFormationName.ApiGateway"
  type = "String"
  description = "The Cloudformation stack that created the app builder API gateway."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "AppBuilderApiGateway"
}

resource "octopusdeploy_variable" "cloudformation_apigateway_stage" {
  name = "CloudFormationName.ApiGatewayStage"
  type = "String"
  description = "The Cloudformation stack that created the app builder API gateway stage."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "AppBuilderApiGatewayStage"
}

resource "octopusdeploy_variable" "config_json_branch" {
  name = "branch"
  type = "String"
  description = "The name of the branch to be inserted into the config.json file."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "#{if WebApp.SubPath}#{WebApp.SubPath}#{/if}#{unless WebApp.SubPath}main#{/unless}"
}