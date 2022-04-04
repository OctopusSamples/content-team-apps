resource "octopusdeploy_project" "deploy_backend_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys the backend service to ECS."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_application_lifecycle_id
  name                                 = "Create ECS Backend Service"
  project_group_id                     = octopusdeploy_project_group.backend_project_group.id
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

resource "octopusdeploy_variable" "aws_account_deploy_backend_project" {
  name     = "AWS Account"
  type     = "AmazonWebServicesAccount"
  value    = var.octopus_aws_account_id
  owner_id = octopusdeploy_project.deploy_infrastructure_project.id
}

locals {
  package_name = "backend"
}

resource "octopusdeploy_deployment_process" "deploy_backend" {
  project_id = octopusdeploy_project.deploy_backend_project.id
  step {
    condition           = "Success"
    name                = "Deploy Application Lambda"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Deploy Application Lambda"
      notes          = "Deploy the task definition and service via CloudFormation"
      run_on_server  = true
      worker_pool_id = var.octopus_worker_pool_id
      environments   = [
        var.octopus_production_environment_id, var.octopus_development_environment_id
      ]
      package {
        name                      = local.package_name
        package_id                = var.octopus_docker_image
        feed_id                   = var.octopus_k8s_feed_id
        acquisition_location      = "NotAcquired"
        extract_during_deployment = false
      }

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : "[]"
        "Octopus.Action.Aws.CloudFormationStackName" : "AppBuilder-ECS-Task"
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          AWSTemplateFormatVersion: '2010-09-09'
          Resources:
            ServiceBackend:
              Type: AWS::ECS::Service
              Properties:
                Cluster:
                  Ref: ClusterName
                TaskDefinition:
                  Ref: TaskDefinitionBackend
                DesiredCount: 1
                EnableECSManagedTags: false
                Tags: []
                LaunchType: FARGATE
                NetworkConfiguration:
                  AwsvpcConfiguration:
                    AssignPublicIp: ENABLED
                    SecurityGroups:
                      - !Ref SecurityGroup
                    Subnets:
                      - !Ref SubnetA
                      - !Ref SubnetB
                DeploymentConfiguration:
                  MaximumPercent: 200
                  MinimumHealthyPercent: 100
              DependsOn: TaskDefinitionbackend
            TaskDefinitionBackend:
              Type: AWS::ECS::TaskDefinition
              Properties:
                ContainerDefinitions:
                  - Essential: true
                    Image: '#{Octopus.Action.Package[${local.package_name}].Image}'
                    Name: backend
                    ResourceRequirements: []
                    EnvironmentFiles: []
                    DisableNetworking: false
                    DnsServers: []
                    DnsSearchDomains: []
                    ExtraHosts: []
                    PortMappings:
                      - ContainerPort: 80
                        HostPort: 80
                        Protocol: tcp
                Family:
                  Ref: TaskDefinitionName
                Cpu:
                  Ref: TaskDefinitionCPU
                Memory:
                  Ref: TaskDefinitionMemory
                ExecutionRoleArn:
                  Ref: TaskExecutionRolebackend
                RequiresCompatibilities:
                  - FARGATE
                NetworkMode: awsvpc
                Volumes: []
                Tags: []
                RuntimePlatform:
                  CPUArchitecture: X86_64
                  OperatingSystemFamily: LINUX
            TaskExecutionRolebackend:
              Type: AWS::IAM::Role
              Properties:
                AssumeRolePolicyDocument:
                  Version: '2012-10-17'
                  Statement:
                    - Effect: Allow
                      Principal:
                        Service:
                          - ecs-tasks.amazonaws.com
                      Action:
                        - sts:AssumeRole
                Path: /
                ManagedPolicyArns:
                  - Ref: AmazonECSTaskExecutionRolePolicyArn
          Parameters:
            ClusterName:
              Type: String
              Default: app-builder
            TaskDefinitionName:
              Type: String
              Default: backend
            TaskDefinitionCPU:
              Type: String
              Default: '256'
            TaskDefinitionMemory:
              Type: String
              Default: '512'
            AmazonECSTaskExecutionRolePolicyArn:
              Type: String
            SubnetA:
              Type: String
            SubnetB:
              Type: String
            SecurityGroup:
              Type: String
            EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : "[]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : "[]"
        "Octopus.Action.Aws.IamCapabilities" : "[\"CAPABILITY_AUTO_EXPAND\",\"CAPABILITY_IAM\",\"CAPABILITY_NAMED_IAM\"]"
        "Octopus.Action.Aws.Region" : "#{AWS.Region}"
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS.Account"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Check for Vulnerabilities"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    run_script_action {
      can_be_used_for_project_versioning = false
      condition                          = "Success"
      is_disabled                        = false
      is_required                        = true
      script_syntax                      = "Bash"
      script_source                      = "Inline"
      run_on_server                      = true
      worker_pool_id                     = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      name                               = "Check for Vulnerabilities"
      environments                       = [
        data.octopusdeploy_environments.development_security.environments[0].id,
        data.octopusdeploy_environments.production_security.environments[0].id
      ]
      package {
        name                      = local.package_name
        package_id                = "quarkus-microservice-sbom"
        feed_id                   = var.octopus_built_in_feed_id
        acquisition_location      = "Server"
        extract_during_deployment = true
      }
      script_body = <<-EOT
          TIMESTAMP=$(date +%s%3N)
          SUCCESS=0
          for x in **/bom.xml; do
              # Delete any existing report file
              if [[ -f "$PWD/depscan-bom.json" ]]; then
                rm "$PWD/depscan-bom.json"
              fi

              # Generate the report, capturing the output, and ensuring $? is set to the exit code
              OUTPUT=$(bash -c "docker run --rm -v \"$PWD:/app\" appthreat/dep-scan scan --bom \"/app/bom.xml\" --type bom --report_file /app/depscan.json; exit \$?" 2>&1)

              # Success is set to 1 if the exit code is not zero
              if [[ $? -ne 0 ]]; then
                  SUCCESS=1
              fi

              # Report file is not generated if no threats found
              # https://github.com/ShiftLeftSecurity/sast-scan/issues/168
              if [[ -f "$PWD/depscan-bom.json" ]]; then
                new_octopusartifact "$PWD/depscan-bom.json"
                # The number of lines in the report file equals the number of vulnerabilities found
                COUNT=$(wc -l < "$PWD/depscan-bom.json")
              else
                COUNT=0
              fi

              # Print the output stripped of ANSI colour codes
              echo -e "$${OUTPUT}" | sed 's/\x1b\[[0-9;]*m//g'
          done

          set_octopusvariable "VerificationResult" $SUCCESS

          exit 0
        EOT
    }
  }
}