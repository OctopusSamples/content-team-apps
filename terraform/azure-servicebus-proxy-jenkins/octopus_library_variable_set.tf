resource "octopusdeploy_library_variable_set" "library_variable_set" {
  name = "Jenkins Pipelines Azure Service Bus Proxy"
  description = "Variables used when deploying the Jenkins Pipelines Azure Service Bus Proxy"
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
  value = "AzureServiceBusProxyJenkinsBucket"
}

resource "octopusdeploy_variable" "aws_cloudformation_lambda" {
  name = "CloudFormation.AzureServiceBusProxyLambda"
  type = "String"
  description = "The name of the stack hosting lambda."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "AzureServiceBusProxyJenkinsLambda"
}

resource "octopusdeploy_variable" "aws_cloudformation_lambda_version" {
  name = "CloudFormation.AzureServiceBusProxyLambdaVersion"
  type = "String"
  description = "The name of the stack hosting lambda."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "AzureServiceBusProxyJenkinsLambdaVersion"
}

resource "octopusdeploy_variable" "aws_cloudformation_lambda_reverse_proxy" {
  name = "CloudFormation.AzureServiceBusProxyLambdaReverseProxy"
  type = "String"
  description = "The name of the stack hosting the reverse proxy lambda."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "AzureServiceBusProxyJenkinsLambdaReverseProxy"
}

resource "octopusdeploy_variable" "aws_cloudformation_lambda_reverse_proxy_version" {
  name = "CloudFormation.AzureServiceBusProxyLambdaReverseProxyVersion"
  type = "String"
  description = "The name of the stack hosting the reverse proxy lambda version."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "AzureServiceBusProxyJenkinsLambdaReverseProxyVersion"
}

resource "octopusdeploy_variable" "aws_cloudformation_code" {
  name = "CloudFormation.AzureServiceBusProxy"
  type = "String"
  description = "The name of the stack hosting lambda."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "AzureServiceBusProxyJenkins"
}

resource "octopusdeploy_variable" "aws_cloudformation_cognito_user_pool" {
  name = "CloudFormation.AzureServiceBusProxyCognitoUserPool"
  type = "String"
  description = "The name of the stack hosting lambda."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = "AzureServiceBusProxyJenkinsCognitoUserPool"
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

resource "octopusdeploy_variable" "cloudformation_servicebus_application_id_production" {
  name = "CommercialMessageBus.ApplicationId"
  type = "String"
  description = "The service bus application ID."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.servicebus_application_id_production
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_servicebus_application_id_development" {
  name = "CommercialMessageBus.ApplicationId"
  type = "String"
  description = "The service bus application ID."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.servicebus_application_id_development
  scope {
    environments = [var.octopus_development_security_environment_id, var.octopus_development_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_servicebus_secret_production" {
  name = "CommercialMessageBus.Secret"
  type = "String"
  description = "The azure service bus secret."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.servicebus_secret_production
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_servicebus_secret_development" {
  name = "CommercialMessageBus.Secret"
  type = "String"
  description = "The azure service bus secret."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.servicebus_secret_development
  scope {
    environments = [var.octopus_development_security_environment_id, var.octopus_development_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_servicebus_tenant_production" {
  name = "CommercialMessageBus.TenantId"
  type = "String"
  description = "The azure service bus Tenant ID."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.servicebus_tenant_production
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_servicebus_tenant_development" {
  name = "CommercialMessageBus.TenantId"
  type = "String"
  description = "The azure service bus Tenant ID."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.servicebus_tenant_development
  scope {
    environments = [var.octopus_development_security_environment_id, var.octopus_development_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_servicebus_namespace_production" {
  name = "CommercialMessageBus.Namespace"
  type = "String"
  description = "The azure service bus namespace."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.servicebus_namespace_production
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}

resource "octopusdeploy_variable" "cloudformation_servicebus_namespace_development" {
  name = "CommercialMessageBus.Namespace"
  type = "String"
  description = "The azure service bus namespace."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.library_variable_set.id
  value = var.servicebus_namespace_development
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
  value = "AzureServiceBusProxyJenkinsLambda"
}