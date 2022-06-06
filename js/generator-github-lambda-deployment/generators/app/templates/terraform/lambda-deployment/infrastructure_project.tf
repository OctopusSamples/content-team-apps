resource "octopusdeploy_project" "deploy_infrastructure_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys a shared API Gateway. This project is created and managed by the [Octopus Terraform provider](https://registry.terraform.io/providers/OctopusDeployLabs/octopusdeploy/latest/docs). The Terraform files can be found in the [GitHub repo](https://github.com/${var.github_repo})."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_infrastructure_lifecycle_id
  name                                 = "API Gateway"
  project_group_id                     = octopusdeploy_project_group.infrastructure_project_group.id
  tenanted_deployment_participation    = "Untenanted"
  space_id                             = var.octopus_space_id
  included_library_variable_sets       = []

  connectivity_policy {
    allow_deployments_to_no_targets = false
    exclude_unhealthy_targets       = false
    skip_machine_behavior           = "SkipUnavailableMachines"
  }
}

output "deploy_infrastructure_project" {
  value = octopusdeploy_project.deploy_infrastructure_project.id
}

resource "octopusdeploy_variable" "aws_account_deploy_infrastructure_project" {
  name     = "AWS Account"
  type     = "AmazonWebServicesAccount"
  value    = var.octopus_aws_account_id
  owner_id = octopusdeploy_project.deploy_infrastructure_project.id
}

locals {
  api_gateway_stack = "OctopusBuilder-APIGateway-${lower(var.github_repo_owner)}-#{Octopus.Environment.Name | Replace \" .*\" \"\" | ToLower}"
  api_gateway_cloudformation_tags = "[{\"key\":\"Environment\",\"value\":\"#{Octopus.Environment.Name}\"},{\"key\":\"DeploymentProject\",\"value\":\"API_Gateway\"}]"
}

resource "octopusdeploy_deployment_process" "deploy_cluster" {
  project_id = octopusdeploy_project.deploy_infrastructure_project.id
  step {
    condition           = "Success"
    name                = "Create API Gateway"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Create API Gateway"
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole": "False"
        "Octopus.Action.Aws.CloudFormation.Tags": local.api_gateway_cloudformation_tags
        "Octopus.Action.Aws.CloudFormationStackName": local.api_gateway_stack
        "Octopus.Action.Aws.CloudFormationTemplate": <<-EOT
          Resources:
            RestApi:
              Type: 'AWS::ApiGateway::RestApi'
              Properties:
                Description: My API Gateway
                Name: Octopus Workflow Builder
                BinaryMediaTypes:
                  - '*/*'
                EndpointConfiguration:
                  Types:
                    - REGIONAL
            Health:
              Type: 'AWS::ApiGateway::Resource'
              Properties:
                RestApiId:
                  Ref: RestApi
                ParentId:
                  'Fn::GetAtt':
                    - RestApi
                    - RootResourceId
                PathPart: health
            Api:
              Type: 'AWS::ApiGateway::Resource'
              Properties:
                RestApiId:
                  Ref: RestApi
                ParentId:
                  'Fn::GetAtt':
                    - RestApi
                    - RootResourceId
                PathPart: api
          Outputs:
            RestApi:
              Description: The REST API
              Value:
                Ref: RestApi
            RootResourceId:
              Description: ID of the resource exposing the root resource id
              Value:
                'Fn::GetAtt':
                  - RestApi
                  - RootResourceId
            Health:
              Description: ID of the resource exposing the health endpoints
              Value:
                Ref: Health
            Api:
              Description: ID of the resource exposing the api endpoint
              Value:
                Ref: Api
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters": "[]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw": "[]"
        "Octopus.Action.Aws.Region": var.aws_region
        "Octopus.Action.Aws.TemplateSource": "Inline"
        "Octopus.Action.Aws.WaitForCompletion": "True"
        "Octopus.Action.AwsAccount.UseInstanceRole": "False"
        "Octopus.Action.AwsAccount.Variable": "AWS Account"
      }
    }
  }
}