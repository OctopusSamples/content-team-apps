resource "octopusdeploy_library_variable_set" "library_variable_set" {
  name = "App Builder Octopus Oauth Proxy"
  description = "Variables used when deploying the App Builder Octopus Oauth Proxy"
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
  description = "The name of the stack creating the App Builder Octopus OAuth proxy S3 bucket."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "AppBuilderOctopusOAuthProxy"
}

resource "octopusdeploy_variable" "aws_cloudformation_code" {
  name = "CloudFormation.BackendCodeExchangeStack"
  type = "String"
  description = "The name of the stack hosting the lambda that converts a code to a token."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "AppBuilderOctopusOAuthProxyCode"
}

resource "octopusdeploy_variable" "aws_cloudformation_login" {
  name = "CloudFormation.BackendLoginStack"
  type = "String"
  description = "The name of the stack hosting the lambda that initiates the OAuth login."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "AppBuilderOctopusOAuthProxyLogin"
}

resource "octopusdeploy_variable" "cloudformation_apigateway" {
  name = "CloudFormationName.ApiGateway"
  type = "String"
  description = "The Cloudformation stack that created the app builder API gateway."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "AppBuilderApiGateway"
}

resource "octopusdeploy_variable" "cloudformation_apigateway_stage" {
  name = "CloudFormationName.ApiGatewayStage"
  type = "String"
  description = "The Cloudformation stack that created the app builder API gateway stage."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "AppBuilderApiGatewayStage"
}

resource "octopusdeploy_variable" "cloudformation_lambda_login" {
  name = "Lambda.LoginName"
  type = "String"
  description = "The name of the Lambda that redirects users to the GitHub OAuth login."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "OctopusOauthProxyLoginRedirect"
}

resource "octopusdeploy_variable" "cloudformation_lambda_code" {
  name = "Lambda.TokenExchangeName"
  type = "String"
  description = "The name of the Lambda that encrypts the returned ID token and passes it back to the client in a cookie."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "OctopusOauthProxyCodeExchange"
}

resource "octopusdeploy_variable" "cloudformation_code_url" {
  name = "Client.ClientRedirect"
  type = "String"
  description = "The URL that proxy will send users back to once the OAuth token has been retrieved."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "https://o9rot8lk3g.execute-api.us-west-1.amazonaws.com/Development/"
}

resource "octopusdeploy_variable" "cloudformation_login_redirect" {
  name = "Octopus.LoginRedirect"
  type = "String"
  description = "The URL that GitHub will call with the OAuth code."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "https://o9rot8lk3g.execute-api.us-west-1.amazonaws.com/Development/oauth/octopus/response"
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
  description = "The salt used to when encrypting the GitHub Oauth token sent back to the client in a cookie."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "6gT9QfxE27MJ1tvrQMRsbaKAi3xHLQLD"
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_encryption_salt_development" {
  name = "Client.EncryptionSalt"
  type = "String"
  description = "The salt used to when encrypting the GitHub Oauth token sent back to the client in a cookie."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "Uf5fxS2q7djbvoRRCuwUKbPCcxVDgh49"
  scope {
    environments = [var.octopus_development_security_environment_id, var.octopus_development_environment_id]
  }
}