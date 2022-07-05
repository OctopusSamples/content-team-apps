resource "octopusdeploy_project" "deploy_backend_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys a CloudFormation template to create an ECR repository."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  name                                 = var.octopus_project_name
  lifecycle_id                         = var.octopus_lifecycle_id
  project_group_id                     = data.octopusdeploy_project_groups.apprunner_project_group.project_groups[0].id
  tenanted_deployment_participation    = "Untenanted"
  space_id                             = var.octopus_space_id
  included_library_variable_sets       = []
  versioning_strategy {
    template = "#{Octopus.Version.LastMajor}.#{Octopus.Version.LastMinor}.#{Octopus.Version.LastPatch}.#{Octopus.Version.NextRevision}"
  }

  connectivity_policy {
    allow_deployments_to_no_targets = false
    exclude_unhealthy_targets       = false
    skip_machine_behavior           = "SkipUnavailableMachines"
  }
}

output "deploy_backend_project_id" {
  value = octopusdeploy_project.deploy_backend_project.id
}

resource "octopusdeploy_variable" "debug_variable" {
  name         = "OctopusPrintVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_backend_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "debug_evaluated_variable" {
  name         = "OctopusPrintEvaluatedVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_backend_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "aws_development_account_deploy_backend_project" {
  name     = "AWS Account"
  type     = "AmazonWebServicesAccount"
  value    = var.octopus_aws_development_account_id
  owner_id = octopusdeploy_project.deploy_backend_project.id
  scope {
    environments = [
      var.octopus_development_environment_id,
      var.octopus_development_security_environment_id,
    ]
  }
}

resource "octopusdeploy_variable" "aws_production_account_deploy_backend_project" {
  name     = "AWS Account"
  type     = "AmazonWebServicesAccount"
  value    = var.octopus_aws_production_account_id
  owner_id = octopusdeploy_project.deploy_backend_project.id
  scope {
    environments = [
      var.octopus_production_environment_id,
      var.octopus_production_security_environment_id,
    ]
  }
}

locals {
  cloudformation_tags = jsonencode([
    {
      key : "OctopusTransient"
      value : "True"
    },
    {
      key : "OctopusTenantId"
      value : "#{if Octopus.Deployment.Tenant.Id}#{Octopus.Deployment.Tenant.Id}#{/if}#{unless Octopus.Deployment.Tenant.Id}untenanted#{/unless}"
    },
    {
      key : "OctopusStepId"
      value : "#{Octopus.Step.Id}"
    },
    {
      key : "OctopusRunbookRunId"
      value : "#{if Octopus.RunBookRun.Id}#{Octopus.RunBookRun.Id}#{/if}#{unless Octopus.RunBookRun.Id}none#{/unless}"
    },
    {
      key : "OctopusDeploymentId"
      value : "#{if Octopus.Deployment.Id}#{Octopus.Deployment.Id}#{/if}#{unless Octopus.Deployment.Id}none#{/unless}"
    },
    {
      key : "OctopusProjectId"
      value : "#{Octopus.Project.Id}"
    },
    {
      key : "OctopusEnvironmentId"
      value : "#{Octopus.Environment.Id}"
    },
    {
      key : "Environment"
      value : "#{Octopus.Environment.Name | Replace \" .*\" \"\"}"
    },
    {
      key : "DeploymentProject"
      value : "ECR_Repository"
    }
  ])
}

resource "octopusdeploy_deployment_process" "deploy_backend" {
  project_id = octopusdeploy_project.deploy_backend_project.id


  step {
    condition           = "Success"
    name                = "Deploy ECR Repository"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Deploy ECR Repository"
      notes          = "Create an ECR repository with CloudFormation."
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        var.octopus_development_environment_id,
        var.octopus_production_environment_id
      ]
      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : local.cloudformation_tags
        "Octopus.Action.Aws.CloudFormationStackName" : var.cloudformation_stack_name
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          Parameters:
            RepositoryName:
              Type: String
          Resources:
            Repository:
              Type: AWS::ECR::Repository
              Properties:
                RepositoryName: !Ref RepositoryName
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : jsonencode([])
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : jsonencode([])
        "Octopus.Action.Aws.IamCapabilities" : jsonencode([])
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
      }
    }
  }
}