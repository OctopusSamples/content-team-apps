resource "octopusdeploy_project" "deploy_backend_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys the backend service to App Runner."
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

resource "octopusdeploy_variable" "development_backend_service" {
  name     = "templateGeneratorHost"
  type     = "String"
  value    = "https://#{Octopus.Action[Get Stack Outputs].Output.BackendUrl}"
  owner_id = octopusdeploy_project.deploy_backend_project.id
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
      value : "App_Runner_Service"
    }
  ])
}

resource "octopusdeploy_deployment_process" "deploy_backend" {
  project_id = octopusdeploy_project.deploy_backend_project.id

  step {
    condition           = "Success"
    name                = "Get Stack Outputs"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Get Stack Outputs"
      notes          = "This step extracts values from previously deployed CloudFormation stacks."
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        var.octopus_development_environment_id,
        var.octopus_production_environment_id
      ]
      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
        "Octopus.Action.Script.ScriptBody" : <<-EOT
          echo "Downloading Docker images"

          echo "##octopus[stdout-verbose]"

          docker pull amazon/aws-cli 2>&1

          # Alias the docker run commands
          shopt -s expand_aliases
          alias aws="docker run --rm -i -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY amazon/aws-cli"

          echo "##octopus[stdout-default]"

          PRIVATE_ECR=$(aws cloudformation \
              describe-stacks \
              --stack-name "content-team-customizable-frontend" \
              --query "Stacks[0].Outputs[?OutputKey=='RepositoryUri'].OutputValue" \
              --output text)

          set_octopusvariable "PrivateEcr" $${PRIVATE_ECR}

          if [[ -z "$${PRIVATE_ECR}" ]]; then
            echo "Please run the Content Team Template Customizable Frontend project first"
            exit 1
          fi

          echo "Private ECR Repo: $${PRIVATE_ECR}"

          BACKEND_URL=$(aws cloudformation \
              describe-stacks \
              --stack-name "content-team-template-generator-apprunner" \
              --query "Stacks[0].Outputs[?OutputKey=='ServiceUrl'].OutputValue" \
              --output text)

          set_octopusvariable "BackendUrl" $${BACKEND_URL}

          if [[ -z "$${BACKEND_URL}" ]]; then
            echo "Please run the Template Frontend App Runner project first"
            exit 1
          fi

          echo "Backend Service URL: $${BACKEND_URL}"
        EOT
        "Octopus.Action.Script.ScriptSource" : "Inline"
        "Octopus.Action.Script.Syntax" : "Bash"
        "OctopusUseBundledTooling" : "False"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Build Environment Image"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Build Environment Image"
      notes          = "This step builds and pushes an environment specific image."
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        var.octopus_development_environment_id,
        var.octopus_production_environment_id
      ]
      features = ["Octopus.Features.JsonConfigurationVariables"]
      package {
        name                      = "customizable-workflow-builder-frontend-config"
        package_id                = "customizable-workflow-builder-frontend-config"
        feed_id                   = var.octopus_built_in_feed_id
        acquisition_location      = "Server"
        extract_during_deployment = true
      }
      properties = {
        "Octopus.Action.Package.JsonConfigurationVariablesTargets": "**/config.json"
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
        "Octopus.Action.Script.ScriptBody" : <<-EOT
          echo "Downloading Docker images"

          echo "##octopus[stdout-verbose]"

          docker pull amazon/aws-cli 2>&1

          # Alias the docker run commands
          shopt -s expand_aliases
          alias aws="docker run --rm -i -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY amazon/aws-cli"

          echo "##octopus[stdout-default]"

          # Build and push the environment specific image
          PRIVATE_ECR="#{Octopus.Action[Get Stack Outputs].Output.PrivateEcr}"

          # Log into ECR using bash to strip the repo name from the private ecr RepositoryUri
          aws ecr get-login-password | docker login --username AWS --password-stdin $${PRIVATE_ECR%/*} 2>&1

          # Build and push the image
          echo "Build environment specific image"

          echo "##octopus[stdout-verbose]"

          cd customizable-workflow-builder-frontend-config

          # Note we need to build a new image tage each time, as App Runner does not appear
          # to re-download the image if the tag does not change. So the environment specific
          # image is tagged with the deployment ID.
          docker build -f Dockerfile.environment -t $${PRIVATE_ECR}:#{Octopus.Deployment.Id | ToLower} .
          docker push $${PRIVATE_ECR}#{Octopus.Deployment.Id | ToLower}

          echo "##octopus[stdout-default]"
        EOT
        "Octopus.Action.Script.ScriptSource" : "Inline"
        "Octopus.Action.Script.Syntax" : "Bash"
        "OctopusUseBundledTooling" : "False"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Deploy App Runner Instance"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Deploy App Runner Instance"
      notes          = "Deploy the image to an App Runner instance with CloudFormation."
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
            ServiceName:
              Type: String
            ImageIdentifier:
              Type: String
            ImageRepositoryType:
              Type: String
              Default: ECR
            Port:
              Type: String
            CPU:
              Type: String
              Default: 1024
            Memory:
              Type: String
              Default: 2048
          Conditions:
            PrivateECR: !Equals
              - !Ref ImageRepositoryType
              - ECR
          Resources:
            AccessRole:
              Type: AWS::IAM::Role
              Condition: PrivateECR
              Properties:
                AssumeRolePolicyDocument:
                  Version: '2008-10-17'
                  Statement:
                    - Effect: Allow
                      Principal:
                        Service:
                          - build.apprunner.amazonaws.com
                      Action: sts:AssumeRole
                ManagedPolicyArns:
                  - arn:aws:iam::aws:policy/service-role/AWSAppRunnerServicePolicyForECRAccess
            AppRunner:
              Type: 'AWS::AppRunner::Service'
              Properties:
                InstanceConfiguration:
                  Cpu: !Ref CPU
                  Memory: !Ref Memory
                ServiceName: !Ref ServiceName
                SourceConfiguration:
                  AuthenticationConfiguration:
                    AccessRoleArn: !If
                      - PrivateECR
                      - !GetAtt
                        - AccessRole
                        - Arn
                      - !Ref 'AWS::NoValue'
                  AutoDeploymentsEnabled: false
                  ImageRepository:
                    ImageConfiguration:
                      Port: !Ref Port
                    ImageIdentifier: !Ref ImageIdentifier
                    ImageRepositoryType: !Ref ImageRepositoryType
          Outputs:
            ServiceUrl:
              Description: The App Runner URL
              Value: !GetAtt
                - AppRunner
                - ServiceUrl
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : jsonencode([
          {
            ParameterKey : "ImageIdentifier"
            ParameterValue : var.docker_image
          },
          {
            ParameterKey : "Port"
            ParameterValue : var.docker_port
          },
          {
            ParameterKey : "ServiceName"
            ParameterValue : var.apprunner_service_name
          }
        ])
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : jsonencode([
          {
            ParameterKey : "ImageIdentifier"
            ParameterValue : var.docker_image
          },
          {
            ParameterKey : "Port"
            ParameterValue : var.docker_port
          },
          {
            ParameterKey : "ServiceName"
            ParameterValue : var.apprunner_service_name
          }
        ])
        "Octopus.Action.Aws.IamCapabilities" : jsonencode([
          "CAPABILITY_AUTO_EXPAND", "CAPABILITY_IAM", "CAPABILITY_NAMED_IAM"
        ])
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
      }
    }
  }
}