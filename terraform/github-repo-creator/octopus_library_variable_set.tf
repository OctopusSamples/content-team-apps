resource "octopusdeploy_library_variable_set" "library_variable_set" {
  name = "Octopus GitHub Repo Creator"
  description = "Variables used when deploying the GitHub Repo Creator"
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

resource "octopusdeploy_variable" "aws_repo_populator_s3_bucket" {
  name = "CloudFormation.RepoPopulatorS3Bucket"
  type = "String"
  description = "The name of the stack creating the GitHub Repo Creator proxy S3 bucket."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "GitHubRepoCreatorBucket"
}

resource "octopusdeploy_variable" "aws_commit-creator_s3_bucket" {
  name = "CloudFormation.CommitCreatorS3Bucket"
  type = "String"
  description = "The name of the stack creating the GitHub Repo Creator proxy S3 bucket."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "GitHubRepoCreatorBucket"
}

resource "octopusdeploy_variable" "aws_cloudformation_repo_populator" {
  name = "CloudFormation.OctopusPopulateGithubRepo"
  type = "String"
  description = "The name of the stack hosting lambda that populates GitHub repos."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "GitHubRepoCreator"
}

resource "octopusdeploy_variable" "aws_cloudformation_commit_creator" {
  name = "CloudFormation.OctopusCreateGithubCommit"
  type = "String"
  description = "The name of the stack hosting lambda that creates GitHub commits."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "GitHubCreateCommit"
}

resource "octopusdeploy_variable" "aws_cloudformation_commit_creator_app_client" {
  name = "CloudFormation.OctopusCreateGithubCommitAppClient"
  type = "String"
  description = "The name of the stack creating the Cognito app client."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "GitHubCreateCommitAppClient"
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
  value = var.github_proxy_encryption_key_production
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
  value = var.github_proxy_encryption_key_development
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
  value = "95FrLIDR5jFroc3MTD5QBd1EsnYKUh1e"
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
  value = "GitHubRepoCreatorLambda"
}

resource "octopusdeploy_variable" "cloudformation_lambda_create_commit" {
  name = "Lambda.GitHubCommitCreatorName"
  type = "String"
  description = "The name of the Lambda."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "GitHubCommitCreatorLambda"
}

resource "octopusdeploy_variable" "cloudformation_service_disable" {
  name = "Service.Disable"
  type = "String"
  description = "Set to true to disable the service and return an empty value."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "False"
}

resource "octopusdeploy_variable" "cloudformation_externalservice_templategenerator" {
  name = "ExternalService.TemplateGenerator"
  type = "String"
  description = "The URL of the template generation service."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "https://octopusworkflowbuilder-test.octopus.com"
  scope {
    environments = [var.octopus_development_security_environment_id, var.octopus_development_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_externalservice_templategenerator_prod" {
  name = "ExternalService.TemplateGenerator"
  type = "String"
  description = "The URL of the template generation service."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "https://octopusworkflowbuilder.octopus.com"
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_externalservice_repo_populator" {
  name = "ExternalService.RepoPopulator"
  type = "String"
  description = "The URL of the repo population service."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "https://octopusworkflowbuilder-test.octopus.com"
  scope {
    environments = [var.octopus_development_security_environment_id, var.octopus_development_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_externalservice_repo_populator_prod" {
  name = "ExternalService.RepoPopulator"
  type = "String"
  description = "The URL of the repo population service."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "https://octopusworkflowbuilder.octopus.com"
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_client_private_key_base64_production" {
  name = "Client.ClientPrivateKey"
  type = "String"
  description = "The base 64 copy of the private key matching the public key used by the client to encrypt data."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.client_private_key_base64_production
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_client_private_key_base64_development" {
  name = "Client.ClientPrivateKey"
  type = "String"
  description = "The base 64 copy of the private key matching the public key used by the client to encrypt data."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.client_private_key_base64_development
  scope {
    environments = [var.octopus_development_security_environment_id, var.octopus_development_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_audit_client_secret_production" {
  name = "Cognito.GitHubCommitCreatorAuditClientSecret"
  type = "String"
  description = "The Cognito client secret used to authenticate with the audit service."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.audit_client_secret_production
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_audit_client_secret_development" {
  name = "Cognito.GitHubCommitCreatorAuditClientSecret"
  type = "String"
  description = "The Cognito client secret used to authenticate with the audit service."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.audit_client_secret_development
  scope {
    environments = [var.octopus_development_security_environment_id, var.octopus_development_environment_id]
  }
}