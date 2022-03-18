resource "octopusdeploy_project" "cognito_userpool_client_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys the GitHub Actions Azure Service Bus Proxy Cognito User Pool Client. Don't edit this process directly - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_application_lifecycle_id
  name                                 = "GitHub Actions Azure Service Bus Proxy Cognito User Pool Client"
  project_group_id                     = octopusdeploy_project_group.appbuilder_github_oauth_project_group.id
  tenanted_deployment_participation    = "Untenanted"
  space_id                             = var.octopus_space_id
  included_library_variable_sets       = [octopusdeploy_library_variable_set.library_variable_set.id, var.cognito_library_variable_set_id, var.content_team_library_variable_set_id]

  connectivity_policy {
    allow_deployments_to_no_targets = false
    exclude_unhealthy_targets       = false
    skip_machine_behavior           = "SkipUnavailableMachines"
  }
}

output "cognito_userpool_client_project_id" {
  value = octopusdeploy_project.cognito_userpool_client_project.id
}

resource "octopusdeploy_variable" "cognito_userpool_client_project_debug_variable" {
  name = "OctopusPrintVariables"
  type = "String"
  description = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id = octopusdeploy_project.cognito_userpool_client_project.id
  value = "False"
}

resource "octopusdeploy_variable" "cognito_userpool_client_project_debug_evaluated_variable" {
  name = "OctopusPrintEvaluatedVariables"
  type = "String"
  description = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id = octopusdeploy_project.cognito_userpool_client_project.id
  value = "False"
}

resource "octopusdeploy_deployment_process" "cognito_userpool_client_deploy_project" {
  project_id = octopusdeploy_project.cognito_userpool_client_project.id
  step {
    condition           = "Success"
    name                = "Get Stack Outputs"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Get Stack Outputs"
      run_on_server  = true
      worker_pool_id = var.octopus_worker_pool_id
      environments = [var.octopus_production_environment_id, var.octopus_development_environment_id]

      properties = {
        "Octopus.Action.Aws.AssumeRole": "False"
        "Octopus.Action.Aws.Region": "#{AWS.Region}"
        "Octopus.Action.AwsAccount.UseInstanceRole": "False"
        "Octopus.Action.AwsAccount.Variable": "AWS.Account"
        "Octopus.Action.Script.ScriptBody": <<-EOT
          COGNITO_POOL_ID=$(aws cloudformation \
              describe-stacks \
              --stack-name #{CloudFormation.Cognito} \
              --query "Stacks[0].Outputs[?OutputKey=='CognitoUserPoolID'].OutputValue" \
              --output text)
          echo "Cognito Pool ID: $${COGNITO_POOL_ID}"
          set_octopusvariable "CognitoPoolId" $${COGNITO_POOL_ID}
        EOT
        "Octopus.Action.Script.ScriptSource": "Inline"
        "Octopus.Action.Script.Syntax": "Bash"
        "OctopusUseBundledTooling": "False"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Deploy Azure Service Bus Proxy User Pool Client"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Deploy Azure Service Bus Proxy User Pool Client"
      run_on_server  = true
      worker_pool_id = var.octopus_worker_pool_id
      environments = [var.octopus_production_environment_id, var.octopus_development_environment_id]

      properties = {
        "Octopus.Action.Aws.AssumeRole": "False"
        "Octopus.Action.Aws.CloudFormation.Tags": "[{\"key\":\"Environment\",\"value\":\"#{Octopus.Environment.Name}\"},{\"key\":\"Deployment Project\",\"value\":\"GitHub OAuth Backend\"},{\"key\":\"Team\",\"value\":\"Content Marketing\"}]"
        "Octopus.Action.Aws.CloudFormationStackName": "#{CloudFormation.AzureServiceBusProxyCognitoUserPool}"
        "Octopus.Action.Aws.CloudFormationTemplate": <<-EOT
          AWSTemplateFormatVersion: "2010-09-09"
          Parameters:
            CognitoPoolId:
              Type: String
            CallbackUrl:
              Type: String
            ResourceServerIdentifier:
              Type: String
            ResourceServerName:
              Type: String
            ScopeDescription:
              Type: String
            ScopeName:
              Type: String
          Resources:
            ResourceServer:
              Type: AWS::Cognito::UserPoolResourceServer
              Properties:
                Identifier: !Ref ResourceServerIdentifier
                Name: !Ref ResourceServerName
                Scopes:
                  - ScopeDescription: !Ref ScopeDescription
                    ScopeName: !Ref ScopeName
                UserPoolId: !Ref CognitoPoolId
            UserPoolClient:
              Type: AWS::Cognito::UserPoolClient
              Properties:
                UserPoolId: !Ref CognitoPoolId
                AllowedOAuthFlowsUserPoolClient: true
                CallbackURLs:
                  - !Ref CallbackUrl
                AllowedOAuthFlows:
                  - client_credentials
                AllowedOAuthScopes:
                  - !Sub $${ResourceServerIdentifier}/$${ScopeName}
                SupportedIdentityProviders:
                  - COGNITO
                GenerateSecret: true
          Outputs:
            CognitoAppClientID:
              Value: !Ref UserPoolClient
              Description: The app client
            EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters": "[{\"ParameterKey\":\"CognitoPoolId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.CognitoPoolId}\"},{\"ParameterKey\":\"CallbackUrl\",\"ParameterValue\":\"https://example.org\"},{\"ParameterKey\":\"ResourceServerIdentifier\",\"ParameterValue\":\"loginmessage.content-team\"},{\"ParameterKey\":\"ResourceServerName\",\"ParameterValue\":\"Github Action Azure Service Bus Proxy Microservice\"},{\"ParameterKey\":\"ScopeDescription\",\"ParameterValue\":\"Github Action Azure Service Bus Proxy administrator\"},{\"ParameterKey\":\"ScopeName\",\"ParameterValue\":\"admin\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw": "[{\"ParameterKey\":\"CognitoPoolId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.CognitoPoolId}\"},{\"ParameterKey\":\"CallbackUrl\",\"ParameterValue\":\"https://example.org\"},{\"ParameterKey\":\"ResourceServerIdentifier\",\"ParameterValue\":\"loginmessage.content-team\"},{\"ParameterKey\":\"ResourceServerName\",\"ParameterValue\":\"Github Action Azure Service Bus Proxy Microservice\"},{\"ParameterKey\":\"ScopeDescription\",\"ParameterValue\":\"Github Action Azure Service Bus Proxy administrator\"},{\"ParameterKey\":\"ScopeName\",\"ParameterValue\":\"admin\"}]"
        "Octopus.Action.Aws.IamCapabilities": "[]"
        "Octopus.Action.Aws.Region": "#{AWS.Region}"
        "Octopus.Action.Aws.TemplateSource": "Inline"
        "Octopus.Action.Aws.WaitForCompletion": "True"
        "Octopus.Action.AwsAccount.UseInstanceRole": "False"
        "Octopus.Action.AwsAccount.Variable": "AWS.Account"
      }
    }
  }
}