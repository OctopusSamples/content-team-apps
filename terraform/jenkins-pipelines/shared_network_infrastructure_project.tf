resource "octopusdeploy_project" "networking_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys the Jenkins Pipelines shared network infrastructure. Don't edit this process directly - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_application_lifecycle_id
  name                                 = "Jenkins Pipelines Shared Network Infrastructure"
  project_group_id                     = octopusdeploy_project_group.project_group.id
  tenanted_deployment_participation    = "Untenanted"
  space_id                             = var.octopus_space_id
  included_library_variable_sets = [
    "LibraryVariableSets-1181", # JenkinsPipelineBuilder
    "LibraryVariableSets-1223", # JenkinsPipelineShared
    "LibraryVariableSets-1243", # AWS Access
    "LibraryVariableSets-1282" # Content Team Apps
  ]

  connectivity_policy {
    allow_deployments_to_no_targets = false
    exclude_unhealthy_targets       = false
    skip_machine_behavior           = "SkipUnavailableMachines"
  }
}

resource "octopusdeploy_variable" "networking_debug_variable" {
  name = "OctopusPrintVariables"
  type = "String"
  description = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id = octopusdeploy_project.networking_project.id
  value = "False"
}

resource "octopusdeploy_variable" "networking_debug_evaluated_variable" {
  name = "OctopusPrintEvaluatedVariables"
  type = "String"
  description = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id = octopusdeploy_project.networking_project.id
  value = "False"
}

resource "octopusdeploy_deployment_process" "networking_project" {
  project_id = octopusdeploy_project.networking_project.id
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
                Description: My API Gateway
                Name: Jenkins Pipelines API
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
            ApiPipeline:
              Type: 'AWS::ApiGateway::Resource'
              Properties:
                RestApiId: !Ref RestApi
                ParentId: !Ref Api
                PathPart: pipeline
            ApiPipelineJenkins:
              Type: 'AWS::ApiGateway::Resource'
              Properties:
                RestApiId: !Ref RestApi
                ParentId: !Ref ApiPipeline
                PathPart: jenkins
            ApiPipelineGenerate:
              Type: 'AWS::ApiGateway::Resource'
              Properties:
                RestApiId: !Ref RestApi
                ParentId: !Ref ApiPipelineJenkins
                PathPart: generate
            ApiPipelineOAuth:
              Type: 'AWS::ApiGateway::Resource'
              Properties:
                RestApiId: !Ref RestApi
                ParentId: !GetAtt
                  - RestApi
                  - RootResourceId
                PathPart: oauth
            ApiPipelineOAuthJenkins:
              Type: 'AWS::ApiGateway::Resource'
              Properties:
                RestApiId: !Ref RestApi
                ParentId: !Ref ApiPipelineOAuth
                PathPart: jenkins
          Outputs:
            RestApi:
              Description: The REST API
              Value: !Ref RestApi
            RootResourceId:
              Description: ID of the resource exposing the Jenkins pipeline frontend
              Value: !GetAtt
                - RestApi
                - RootResourceId
            Api:
              Description: ID of the resource exposing the API
              Value: !Ref Api
            Web:
              Description: ID of the resource exposing the Jenkins pipeline frontend
              Value: !Ref Web
            '#{CloudFormation.Output.PipelineEndpointVariableName}':
              Description: ID of the resource exposing the Jenkins Pipelines generate function
              Value: !Ref ApiPipelineGenerate
            '#{CloudFormation.Output.OAuthGithubEndpointVariableName}':
              Description: ID of the resource exposing the GitHub OAuth proxy
              Value: !Ref ApiPipelineOAuthJenkins
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters": "[]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw": "[]"
        "Octopus.Action.Aws.Region": "#{AWS.Region}"
        "Octopus.Action.Aws.TemplateSource": "Inline"
        "Octopus.Action.Aws.WaitForCompletion": "True"
        "Octopus.Action.AwsAccount.UseInstanceRole": "False"
        "Octopus.Action.AwsAccount.Variable": "AWS"
      }
    }
  }

}