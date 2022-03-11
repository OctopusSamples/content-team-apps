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
  name                                 = "Deploy App Builder shared infrastructure"
  project_group_id                     = octopusdeploy_project_group.appbuilder_project_group.id
  tenanted_deployment_participation    = "Untenanted"
  space_id                             = var.octopus_space_id
  included_library_variable_sets       = [octopusdeploy_library_variable_set.frontend_library_variable_set.id]

  connectivity_policy {
    allow_deployments_to_no_targets = false
    exclude_unhealthy_targets       = false
    skip_machine_behavior           = "SkipUnavailableMachines"
  }
}

output "deploy_project_id" {
  value = octopusdeploy_project.deploy_project.id
}

resource "octopusdeploy_deployment_process" "deploy_project" {
  project_id = octopusdeploy_project.deploy_project.id
  step {
    condition           = "Success"
    name                = "Create API Gateway"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Create API Gateway"
      run_on_server  = true
      worker_pool_id = var.octopus_worker_pool_id

      properties = {
        "Octopus.Action.Aws.AssumeRole": "False"
        "Octopus.Action.Aws.CloudFormation.Tags": "[{\"key\":\"Environment\",\"value\":\"#{Octopus.Environment.Name}\"},{\"key\":\"Deployment Project\",\"value\":\"GitHub Actions Shared Network Infrastructure\"},{\"key\":\"Team\",\"value\":\"Content Marketing\"}]"
        "Octopus.Action.Aws.CloudFormationStackName": "#{CloudFormationName.ApiGateway}"
        "Octopus.Action.Aws.CloudFormationTemplate": <<-EOT
          Resources:
            RestApi:
              Type: 'AWS::ApiGateway::RestApi'
              Properties:
                Description: App Builder API Gateway
                Name: App Builder API
                BinaryMediaTypes:
                  - '*/*'
                EndpointConfiguration:
                  Types:
                    - REGIONAL
            Web:
              Type: 'AWS::ApiGateway::Resource'
              Properties:
                RestApiId: !Ref RestApi
                ParentId: !GetAtt
                  - RestApi
                  - RootResourceId
                PathPart: '{proxy+}'
            Api:
              Type: 'AWS::ApiGateway::Resource'
              Properties:
                RestApiId: !Ref RestApi
                ParentId: !GetAtt
                  - RestApi
                  - RootResourceId
                PathPart: api
            ApiOAuth:
              Type: 'AWS::ApiGateway::Resource'
              Properties:
                RestApiId: !Ref RestApi
                ParentId: !GetAtt
                  - RestApi
                  - RootResourceId
                PathPart: oauth
            ApiOAuthGitHub:
              Type: 'AWS::ApiGateway::Resource'
              Properties:
                RestApiId: !Ref RestApi
                ParentId: !Ref ApiOAuth
                PathPart: github
            ApiOAuthOctopus:
              Type: 'AWS::ApiGateway::Resource'
              Properties:
                RestApiId: !Ref RestApi
                ParentId: !Ref ApiOAuth
                PathPart: octopus
          Outputs:
            RestApi:
              Description: The REST API
              Value: !Ref RestApi
            RootResourceId:
              Description: ID of the root resource
              Value: !GetAtt
                - RestApi
                - RootResourceId
            Web:
              Description: ID of the resource exposing the web app frontend
              Value: !Ref Web
            Api:
              Description: ID of the resource exposing the REST API endpoints
              Value: !Ref Api
            ApiOAuth:
              Description: ID of the resource exposing the OAuth proxies
              Value: !Ref ApiOAuth
            ApiOAuthGitHub:
              Description: ID of the resource exposing the GitHub OAuth proxies
              Value: !Ref ApiOAuthGitHub
            ApiOAuthOctopus:
              Description: ID of the resource exposing the Octopus OAuth proxies
              Value: !Ref ApiOAuthOctopus

        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters": "[]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw": "[]"
        "Octopus.Action.Aws.Region": "#{AWS.Region}"
        "Octopus.Action.Aws.TemplateSource": "Inline"
        "Octopus.Action.Aws.WaitForCompletion": "True"
        "Octopus.Action.AwsAccount.UseInstanceRole": "False"
        "Octopus.Action.AwsAccount.Variable": "AWS.Account"
      }
    }
  }
  
}