resource "octopusdeploy_project" "deploy_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys the shared network infrastructure. Don't edit this process directly - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_infrastructure_lifecycle_id
  name                                 = "Cognito infrastructure"
  project_group_id                     = octopusdeploy_project_group.project_group.id
  tenanted_deployment_participation    = "Untenanted"
  space_id                             = var.octopus_space_id
  included_library_variable_sets       = [
    "LibraryVariableSets-1243", # AWS Access
    "LibraryVariableSets-1262", # Cognito
    "LibraryVariableSets-1261" # Google
  ]

  connectivity_policy {
    allow_deployments_to_no_targets = false
    exclude_unhealthy_targets       = false
    skip_machine_behavior           = "SkipUnavailableMachines"
  }
}

output "deploy_project_id" {
  value = octopusdeploy_project.deploy_project.id
}

resource "octopusdeploy_variable" "debug_variable" {
  name = "OctopusPrintVariables"
  type = "String"
  description = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id = octopusdeploy_project.deploy_project.id
  value = "False"
}

resource "octopusdeploy_variable" "debug_evaluated_variable" {
  name = "OctopusPrintEvaluatedVariables"
  type = "String"
  description = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id = octopusdeploy_project.deploy_project.id
  value = "False"
}

resource "octopusdeploy_deployment_process" "deploy_project" {
  project_id = octopusdeploy_project.deploy_project.id
  step {
    condition           = "Success"
    name                = "Deploy Cognito"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Deploy Cognito"
      run_on_server  = true
      worker_pool_id = var.octopus_worker_pool_id

      properties = {
        "Octopus.Action.Aws.AssumeRole": "False"
        "Octopus.Action.Aws.CloudFormation.Tags": "[{\"key\":\"Environment\",\"value\":\"#{Octopus.Environment.Name}\"},{\"key\":\"Deployment Project\",\"value\":\"GitHub Actions Shared Network Infrastructure\"},{\"key\":\"Team\",\"value\":\"Content Marketing\"}]"
        "Octopus.Action.Aws.CloudFormationStackName": "#{CloudFormation.Cognito}"
        "Octopus.Action.Aws.CloudFormationTemplate": <<-EOT
          AWSTemplateFormatVersion: "2010-09-09"

          Parameters:
            CognitoDomain:
              Type: String
              MinLength: 3
              MaxLength: 63
              AllowedPattern: ^[a-z0-9](?:[a-z0-9\-]{0,61}[a-z0-9])?$
              Description: Enter a string. Must be alpha numeric 3-63 in length.

            GoogleClientId:
              Type: String

            GoogleClientSecret:
              Type: String

          Resources:
            UserPool:
              Type: AWS::Cognito::UserPool
              Properties:
                UsernameConfiguration:
                  CaseSensitive: false
                AutoVerifiedAttributes:
                  - email
                UserPoolName: !Sub $${CognitoDomain}-user-pool
                Schema:
                  - Name: email
                    AttributeDataType: String
                    Mutable: true # Email must be mutable: https://stackoverflow.com/questions/50365699/saml-attribute-mapping-for-aws-cognito-signup-or-signin-works-but-not-both
                    Required: true
                  - Name: name
                    AttributeDataType: String
                    Mutable: true
                    Required: true

            UserPoolDomain:
              Type: AWS::Cognito::UserPoolDomain
              Properties:
                Domain: !Ref CognitoDomain
                UserPoolId: !Ref UserPool

            CognitoUserPoolIdentityProvider:
              Type: AWS::Cognito::UserPoolIdentityProvider
              Properties:
                ProviderName: Google
                AttributeMapping:
                  email: email
                  name: name
                ProviderDetails:
                  client_id: !Sub $${GoogleClientId}.apps.googleusercontent.com
                  client_secret: !Ref GoogleClientSecret
                  authorize_scopes: email openid profile
                ProviderType: Google
                UserPoolId:
                  Ref: UserPool

            DeveloperGroup:
              Type: AWS::Cognito::UserPoolGroup
              Properties:
                Description: Represents developers of the application
                GroupName: Developers
                UserPoolId: !Ref UserPool


          Outputs:
            CognitoUserPoolID:
              Value: !Ref UserPool
              Description: The UserPool ID
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters": "[{\"ParameterKey\":\"CognitoDomain\",\"ParameterValue\":\"#{Cognito.Domain}\"},{\"ParameterKey\":\"GoogleClientId\",\"ParameterValue\":\"#{Google.ClientId}\"},{\"ParameterKey\":\"GoogleClientSecret\",\"ParameterValue\":\"#{Google.ClientSecret}\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw": "[{\"ParameterKey\":\"CognitoDomain\",\"ParameterValue\":\"#{Cognito.Domain}\"},{\"ParameterKey\":\"GoogleClientId\",\"ParameterValue\":\"#{Google.ClientId}\"},{\"ParameterKey\":\"GoogleClientSecret\",\"ParameterValue\":\"#{Google.ClientSecret}\"}]"
        "Octopus.Action.Aws.Region": "#{Cognito.Region}"
        "Octopus.Action.Aws.TemplateSource": "Inline"
        "Octopus.Action.Aws.WaitForCompletion": "True"
        "Octopus.Action.AwsAccount.UseInstanceRole": "False"
        "Octopus.Action.AwsAccount.Variable": "AWS"
      }
    }
  }
  
}