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

resource "octopusdeploy_variable" "cognito_redirect_url_development" {
  name = "Cognito.RedirectUrl"
  type = "String"
  description = "The redirect URL when returning from a Cognito login. Don't edit these variables directly - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "https://octopusworkflowbuilder-test.octopus.com/"
  scope {
    environments = [var.octopus_development_environment_id, var.octopus_development_security_environment_id]
  }
}

resource "octopusdeploy_variable" "cognito_redirect_url_production" {
  name = "Cognito.RedirectUrl"
  type = "String"
  description = "The redirect URL when returning from a Cognito login. Don't edit these variables directly - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "https://octopusworkflowbuilder.octopus.com/"
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

resource "octopusdeploy_variable" "aws_cloudformation_cognito" {
  name = "CloudFormation.CognitoUserPool"
  type = "String"
  description = "The name of the stack hosting the frontend API Gateway resources."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "AppBuilderFrontendCognito"
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
  name = "CloudFormationName.AppBuilderApiGateway"
  type = "String"
  description = "The Cloudformation stack that created the app builder API gateway."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "AppBuilderApiGateway"
}

resource "octopusdeploy_variable" "cloudformation_apigateway_stage" {
  name = "CloudFormationName.AppBuilderApiGatewayStage"
  type = "String"
  description = "The Cloudformation stack that created the app builder API gateway stage."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "AppBuilderApiGatewayStage"
}

resource "octopusdeploy_variable" "config_json_disableExternalCalls" {
  name = "disableExternalCalls"
  type = "String"
  description = "The flag that enables remote network calls."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "false"
}

resource "octopusdeploy_variable" "config_json_branch" {
  name = "branch"
  type = "String"
  description = "The name of the branch to be inserted into the config.json file."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "#{if WebApp.SubPath}#{WebApp.SubPath}#{/if}#{unless WebApp.SubPath}main#{/unless}"
}

resource "octopusdeploy_variable" "config_json_githubOauthEndpoint" {
  name = "githubOauthEndpoint"
  type = "String"
  description = "The location of the GitHub login proxy."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "/oauth/github/login"
}

resource "octopusdeploy_variable" "config_json_octopusOauthEndpoint" {
  name = "octofrontOauthEndpoint"
  type = "String"
  description = "The location of the Octopus login proxy."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "/oauth/octopus/login"
}

resource "octopusdeploy_variable" "config_json_serviceAccountEndpoint" {
  name = "serviceAccountEndpoint"
  type = "String"
  description = "The location of the Octopus service creation API."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "/api/serviceaccounts"
}

resource "octopusdeploy_variable" "config_json_githubCommitEndpoint" {
  name = "githubCommitEndpoint"
  type = "String"
  description = "The location of the GitHub Repo Creator API."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "/api/githubcommit"
}

resource "octopusdeploy_variable" "config_json_githubRepoEndpoint" {
  name = "githubRepoEndpoint"
  type = "String"
  description = "The location of the GitHub Repo proxy API."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "/api/githubrepo"
}

resource "octopusdeploy_variable" "config_json_octoSpaceEndpoint" {
  name = "octoSpaceEndpoint"
  type = "String"
  description = "The location of the Octopus space proxy API."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "/api/octopusspace"
}

resource "octopusdeploy_variable" "cognito_development" {
  name = "aws:cognitoLogin"
  type = "String"
  description = "The cognito login page - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "https://content-team-octopus.auth.us-west-1.amazoncognito.com/login?client_id=68sn92d4lgqotokg7oo082ildj&response_type=token&scope=email+openid+profile&redirect_uri=https://octopusworkflowbuilder-test.octopus.com/"
  scope {
    environments = [var.octopus_development_environment_id, var.octopus_development_security_environment_id]
  }
}

resource "octopusdeploy_variable" "cognito_production" {
  name = "aws:cognitoLogin"
  type = "String"
  description = "The cognito login page - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "https://content-team-octopus-production.auth.us-west-1.amazoncognito.com/login?client_id=11knnbr7urleiurg3gvjub9qp8&response_type=token&scope=email+openid+profile&redirect_uri=https://octopusworkflowbuilder.octopus.com/"
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}

resource "octopusdeploy_variable" "audit_development" {
  name = "auditEndpoint"
  type = "String"
  description = "The cognito login page - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "https://b3u42cdd6h.execute-api.us-west-1.amazonaws.com/Development/api/audits"
  scope {
    environments = [var.octopus_development_environment_id, var.octopus_development_security_environment_id]
  }
}

resource "octopusdeploy_variable" "audit_production" {
  name = "auditEndpoint"
  type = "String"
  description = "The cognito login page - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "https://dodwbeqe6g.execute-api.us-west-1.amazonaws.com/Production/api/audits"
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}

resource "octopusdeploy_variable" "google_tag" {
  name = "settings:google:tag"
  type = "String"
  description = "The Google analytics tag - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "GTM-M6BF84M"
}