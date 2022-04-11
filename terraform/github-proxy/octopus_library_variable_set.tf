resource "octopusdeploy_library_variable_set" "library_variable_set" {
  name = "GitHub Proxy"
  description = "Variables used when deploying the GitHub Proxy"
}

output "library_variable_set_id" {
  value = octopusdeploy_library_variable_set.library_variable_set.id
}


resource "octopusdeploy_variable" "aws_development_account" {
  name = "AWS.Account"
  type = "AmazonWebServicesAccount"
  description = "The AWS account used to deploy the application. Don't edit these variables directly - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
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
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
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
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.aws_region
}

resource "octopusdeploy_variable" "aws_s3_bucket" {
  name = "CloudFormation.S3Bucket"
  type = "String"
  description = "The name of the stack creating the GitHub Repo Creator proxy S3 bucket."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "GitHubProxyBucket"
}

resource "octopusdeploy_variable" "aws_cloudformation_lambda" {
  name = "CloudFormation.ApplicationLambda"
  type = "String"
  description = "The name of the stack hosting lambda."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "GitHubProxyLambda"
}

resource "octopusdeploy_variable" "aws_cloudformation_lambda_version" {
  name = "CloudFormation.ApplicationLambdaVersion"
  type = "String"
  description = "The name of the stack hosting the lambda version."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "GitHubProxyLambdaVersion"
}

resource "octopusdeploy_variable" "aws_cloudformation_lambda_reverse_proxy" {
  name = "CloudFormation.ApplicationLambdaReverseProxy"
  type = "String"
  description = "The name of the stack hosting the reverse proxy lambda."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "GitHubProxyLambdaReverseProxy"
}

resource "octopusdeploy_variable" "aws_cloudformation_lambda_reverse_proxy_version" {
  name = "CloudFormation.ApplicationLambdaReverseProxyVersion"
  type = "String"
  description = "The name of the stack hosting the reverse proxy lambda version."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "GitHubProxyLambdaReverseProxyVersion"
}

resource "octopusdeploy_variable" "aws_cloudformation_code" {
  name = "CloudFormation.Application"
  type = "String"
  description = "The name of the stack hosting lambda."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "GitHubProxy"
}

resource "octopusdeploy_variable" "cloudformation_apigateway" {
  name = "CloudFormationName.AppBuilderApiGateway"
  type = "String"
  description = "The Cloudformation stack that created the app builder API gateway."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "AppBuilderApiGateway"
}

resource "octopusdeploy_variable" "cloudformation_apigateway_stage" {
  name = "CloudFormationName.AppBuilderApiGatewayStage"
  type = "String"
  description = "The Cloudformation stack that created the app builder API gateway stage."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "AppBuilderApiGatewayStage"
}

resource "octopusdeploy_variable" "cloudformation_lambda_login" {
  name = "Lambda.Name"
  type = "String"
  description = "The name of the Lambda."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "GitHubProxyLambda"
}