resource "octopusdeploy_project" "cognito_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys the app builder frontend Cognito user pool. Don't edit this process directly - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_application_lifecycle_id
  name                                 = "App Builder Frontend Cognito User Pool"
  project_group_id                     = octopusdeploy_project_group.appbuilder_frontend_project_group.id
  tenanted_deployment_participation    = "Untenanted"
  space_id                             = var.octopus_space_id
  included_library_variable_sets       = [octopusdeploy_library_variable_set.frontend_library_variable_set.id, var.cognito_library_variable_set_id, var.content_team_library_variable_set_id]

  connectivity_policy {
    allow_deployments_to_no_targets = false
    exclude_unhealthy_targets       = false
    skip_machine_behavior           = "SkipUnavailableMachines"
  }
}

output "cognito_project_id" {
  value = octopusdeploy_project.cognito_project.id
}

resource "octopusdeploy_variable" "cognito_debug_variable" {
  name = "OctopusPrintVariables"
  type = "String"
  description = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id = octopusdeploy_project.cognito_project.id
  value = "False"
}

resource "octopusdeploy_variable" "cognito_debug_evaluated_variable" {
  name = "OctopusPrintEvaluatedVariables"
  type = "String"
  description = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id = octopusdeploy_project.cognito_project.id
  value = "False"
}

resource "octopusdeploy_deployment_process" "cognito_project" {
  project_id = octopusdeploy_project.cognito_project.id
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

          REST_API_ID=$(aws cloudformation \
              describe-stacks \
              --stack-name #{CloudFormationName.AppBuilderApiGateway} \
              --query "Stacks[0].Outputs[?OutputKey=='RestApi'].OutputValue" \
              --output text)
          echo "REST API ID: $${REST_API_ID}"
          set_octopusvariable "RestApiID" $${REST_API_ID}
        EOT
        "Octopus.Action.Script.ScriptSource": "Inline"
        "Octopus.Action.Script.Syntax": "Bash"
        "OctopusUseBundledTooling": "False"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Deploy Cognito User Pool"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Deploy Cognito User Pool"
      run_on_server  = true
      worker_pool_id = var.octopus_worker_pool_id
      environments = [var.octopus_production_environment_id, var.octopus_development_environment_id]

      properties = {
        "Octopus.Action.Aws.AssumeRole": "False"
        "Octopus.Action.Aws.CloudFormation.Tags": "[{\"key\":\"Environment\",\"value\":\"#{Octopus.Environment.Name}\"},{\"key\":\"Deployment Project\",\"value\":\"Deploy App Builder Frontend\"},{\"key\":\"Team\",\"value\":\"Content Marketing\"},{\"key\":\"Branch\",\"value\":\"main\"}]"
        "Octopus.Action.Aws.CloudFormationStackName": "#{CloudFormation.CognitoUserPool}"
        "Octopus.Action.Aws.CloudFormationTemplate": <<-EOT
          AWSTemplateFormatVersion: "2010-09-09"
          Parameters:
            CognitoPoolId:
              Type: String
            CallbackUrl:
              Type: String
            CognitoDomain:
              Type: String
            CognitoRegion:
              Type: String
          Resources:
            UserPoolClient:
              Type: AWS::Cognito::UserPoolClient
              Properties:
                UserPoolId: !Ref CognitoPoolId
                AllowedOAuthFlowsUserPoolClient: true
                CallbackURLs:
                  - !Ref CallbackUrl
                AllowedOAuthFlows:
                  - implicit
                AllowedOAuthScopes:
                  - email
                  - openid
                  - profile
                SupportedIdentityProviders:
                  - COGNITO
                  - Google
          Outputs:
            CognitoAppClientID:
              Value: !Ref UserPoolClient
              Description: The app client
            HostedUIURL:
              Value: !Sub https://$${CognitoDomain}.auth.$${CognitoRegion}.amazoncognito.com/login?client_id=$${UserPoolClient}&response_type=code&scope=email+openid+profile&redirect_uri=$${CallbackUrl}
              Description: The hosted UI URL
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters": "[{\"ParameterKey\":\"CognitoPoolId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.CognitoPoolId}\"},{\"ParameterKey\":\"CallbackUrl\",\"ParameterValue\":\"https://#{Octopus.Action[Get Stack Outputs].Output.RestApiID}.execute-api.#{AWS.Region}.amazonaws.com/#{Octopus.Environment.Name}/\"},{\"ParameterKey\":\"CognitoDomain\",\"ParameterValue\":\"#{Cognito.Domain}\"},{\"ParameterKey\":\"CognitoRegion\",\"ParameterValue\":\"#{Cognito.Region}\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw": "[{\"ParameterKey\":\"CognitoPoolId\",\"ParameterValue\":\"#{Octopus.Action[Get Stack Outputs].Output.CognitoPoolId}\"},{\"ParameterKey\":\"CallbackUrl\",\"ParameterValue\":\"https://#{Octopus.Action[Get Stack Outputs].Output.RestApiID}.execute-api.#{AWS.Region}.amazonaws.com/#{Octopus.Environment.Name}/\"},{\"ParameterKey\":\"CognitoDomain\",\"ParameterValue\":\"#{Cognito.Domain}\"},{\"ParameterKey\":\"CognitoRegion\",\"ParameterValue\":\"#{Cognito.Region}\"}]"
        "Octopus.Action.Aws.Region": "#{AWS.Region}"
        "Octopus.Action.Aws.TemplateSource": "Inline"
        "Octopus.Action.Aws.WaitForCompletion": "True"
        "Octopus.Action.AwsAccount.UseInstanceRole": "False"
        "Octopus.Action.AwsAccount.Variable": "AWS.Account"
      }
    }
  }
}