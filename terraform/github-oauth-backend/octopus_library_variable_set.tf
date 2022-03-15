resource "octopusdeploy_library_variable_set" "library_variable_set" {
  name = "App Builder GitHub Oauth Proxy"
  description = "Variables used when deploying the App Builder GitHub Oauth Proxy"
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
  value = "AppBuilderOAuthProxy"
}

resource "octopusdeploy_variable" "aws_cloudformation_code" {
  name = "CloudFormation.BackendCodeExchangeStack"
  type = "String"
  description = "The name of the stack hosting the lambda that converts a code to a token."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "AppBuilderGitHubOAuthProxyCode"
}

resource "octopusdeploy_variable" "aws_cloudformation_login" {
  name = "CloudFormation.BackendLoginStack"
  type = "String"
  description = "The name of the stack hosting the lambda that initiates the OAuth login."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "AppBuilderGitHubOAuthProxyLogin"
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
  value = "AppBuilderOauthProxyLoginRedirect"
}

resource "octopusdeploy_variable" "cloudformation_lambda_code" {
  name = "Lambda.TokenExchangeName"
  type = "String"
  description = "The name of the Lambda that exchanges the OAuth code for a token and returns to the web app."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "AppBuilderOauthProxyCodeExchange"
}

resource "octopusdeploy_variable" "cloudformation_code_url" {
  name = "Client.ClientRedirect"
  type = "String"
  description = "The URL that GitHub will send users back to once the OAuth token has been retrieved."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "https://o9rot8lk3g.execute-api.us-west-1.amazonaws.com/Development/"
}

resource "octopusdeploy_variable" "cloudformation_login_redirect" {
  name = "GitHub.LoginRedirect"
  type = "String"
  description = "The URL that GitHub will call with the OAuth code."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "https://o9rot8lk3g.execute-api.us-west-1.amazonaws.com/Development/oauth/github/code"
}

resource "octopusdeploy_variable" "cloudformation_encryption_key_production" {
  name = "Client.EncryptionKey"
  type = "String"
  description = "The key used to encrypt the GitHub Oauth token sent back to the client in a cookie."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.github_proxy_encryption_key_production
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_encryption_key_development" {
  name = "Client.EncryptionKey"
  type = "String"
  description = "The key used to encrypt the GitHub Oauth token sent back to the client in a cookie."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.github_proxy_encryption_key_development
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
  value = "bMjahk2IHvEVz5XgIaO82SfaHjwGMZQ9"
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
  value = "95FrLIDR5jFroc3MTD5QBd1EsnYKUh1e"
  scope {
    environments = [var.octopus_development_security_environment_id, var.octopus_development_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_github_oauth_client_id_development" {
  name = "GitHub.OAuthAppClientId"
  type = "String"
  description = "The GitHub OAuth app client ID."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.github_proxy_client_id_development
  scope {
    environments = [var.octopus_development_security_environment_id, var.octopus_development_environment_id]
  }
}


resource "octopusdeploy_variable" "cloudformation_github_oauth_client_secret_development" {
  name = "GitHub.OAuthAppClientSecret"
  type = "Sensitive"
  description = "The GitHub OAuth app client secret."
  is_sensitive = true
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.github_proxy_client_secret_development
  scope {
    environments = [var.octopus_development_security_environment_id, var.octopus_development_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_github_oauth_client_id_production" {
  name = "GitHub.OAuthAppClientId"
  type = "String"
  description = "The GitHub OAuth app client ID."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.github_proxy_client_id_production
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}


resource "octopusdeploy_variable" "cloudformation_github_oauth_client_secret_production" {
  name = "GitHub.OAuthAppClientSecret"
  type = "Sensitive"
  description = "The GitHub OAuth app client secret."
  is_sensitive = true
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.github_proxy_client_secret_production
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}