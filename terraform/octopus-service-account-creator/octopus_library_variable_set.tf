resource "octopusdeploy_library_variable_set" "library_variable_set" {
  name = "Octopus Service Account Creator"
  description = "Variables used when deploying the Octopus Service Account Creator"
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
  description = "The name of the stack creating the App Builder GitHub OAuth proxy S3 bucket."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "OctopusServiceAccountCreatorBucket"
}

resource "octopusdeploy_variable" "aws_cloudformation_code" {
  name = "CloudFormation.OctopusServiceAccountCreator"
  type = "String"
  description = "The name of the stack hosting lambda."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "OctopusServiceAccountCreator"
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

resource "octopusdeploy_variable" "cloudformation_encryption_key_production" {
  name = "Client.EncryptionKey"
  type = "String"
  description = "The key used to encrypt the Octopus ID token sent back to the client in a cookie."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.octopus_proxy_encryption_key_production
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_encryption_key_development" {
  name = "Client.EncryptionKey"
  type = "String"
  description = "The key used to encrypt the Octopus ID token sent back to the client in a cookie."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.octopus_proxy_encryption_key_development
  scope {
    environments = [var.octopus_development_security_environment_id, var.octopus_development_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_encryption_salt_production" {
  name = "Client.EncryptionSalt"
  type = "String"
  description = "The salt used to when encrypting the Octopus ID token sent back to the client in a cookie."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "bMjahk2IHvEVz5XgIaO82SfaHjwGMZQ9"
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_encryption_salt_development" {
  name = "Client.EncryptionSalt"
  type = "String"
  description = "The salt used to when encrypting the Octopus ID token sent back to the client in a cookie."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "Uf5fxS2q7djbvoRRCuwUKbPCcxVDgh49"
  scope {
    environments = [var.octopus_development_security_environment_id, var.octopus_development_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_lambda_login" {
  name = "Lambda.Name"
  type = "String"
  description = "The name of the Lambda."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "OctopusCreateAccountServiceLambda"
}

resource "octopusdeploy_variable" "cloudformation_service_disable" {
  name = "Service.Disable"
  type = "String"
  description = "Set to true to disable the service and return an empty value."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "True"
}

resource "octopusdeploy_variable" "octopus_test_api_key" {
  name = "Octopus.TestApiKey"
  type = "String"
  description = "The test api key to return when account creation is disabled."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = ""
  # Set the value to the variable below to have the service creator return a fixed api key.
  # Beware that this will essentially leak an API key into the GitHub repos. This value is
  # only useful for internal testing.
  # value = var.octopus_test_api_key
}

resource "octopusdeploy_variable" "octopus_test_server" {
  name = "Octopus.TestServer"
  type = "String"
  description = "The test server to return when account creation is disabled."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = ""
  # This value is only useful for internal testing.
  # value = var.octopus_test_server
}