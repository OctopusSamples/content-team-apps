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

resource "octopusdeploy_variable" "aws_account_deploy_backend_project" {
  name     = "AWS Account"
  type     = "AmazonWebServicesAccount"
  value    = var.octopus_aws_account_id
  owner_id = octopusdeploy_project.deploy_backend_project.id
}

locals {
  package_name = "backend"
}

resource "octopusdeploy_deployment_process" "deploy_backend" {
  project_id = octopusdeploy_project.deploy_backend_project.id
  step {
    condition           = "Success"
    name                = "Get AWS Resources"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = []
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Get AWS Resources"
      notes          = "Queries AWS for the subnets, security groups, and IAM roles."
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      properties     = {
        "OctopusUseBundledTooling" : "False",
        "Octopus.Action.Script.ScriptSource" : "Inline",
        "Octopus.Action.Script.Syntax" : "Bash",
        "Octopus.Action.Aws.AssumeRole" : "False",
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False",
        "Octopus.Action.AwsAccount.Variable" : "AWS Account",
        "Octopus.Action.Aws.Region" : var.aws_region,
        "Octopus.Action.Script.ScriptBody" : <<-EOT
          # Get the containers
          echo "Downloading Docker images"
          echo "##octopus[stdout-verbose]"
          docker pull amazon/aws-cli 2>&1
          docker pull imega/jq 2>&1
          echo "##octopus[stdout-default]"

          # Alias the docker run commands
          shopt -s expand_aliases
          alias aws="docker run --rm -i -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY amazon/aws-cli"
          alias jq="docker run --rm -i imega/jq"

          EXISTINGROLE=$(aws iam list-roles --max-items 10000 | jq -r '.Roles[] | select(.RoleName == "ecsTaskExecutionRole") | .Arn')
          SUBNETA=$(aws ec2 describe-subnets --filter "Name=tag:aws:cloudformation:stack-name,Values=amazon-ecs-cli-setup-app-builder" | jq -r '.Subnets[0].SubnetId')
          SUBNETB=$(aws ec2 describe-subnets --filter "Name=tag:aws:cloudformation:stack-name,Values=amazon-ecs-cli-setup-app-builder" | jq -r '.Subnets[1].SubnetId')
          VPC=$(aws ec2 describe-vpcs --filter "Name=tag:aws:cloudformation:stack-name,Values=amazon-ecs-cli-setup-app-builder" | jq -r '.Vpcs[0].VpcId')
          SECURITYGROUP=$(aws ec2 describe-security-groups --filters Name=vpc-id,Values=$${VPC} | jq -r '.SecurityGroups[].GroupId')

          echo "Found Role ARN: $${EXISTINGROLE}"
          echo "Found Security Group: $${SECURITYGROUP}"
          echo "Found Subnet A: $${SUBNETA}"
          echo "Found Subnet B: $${SUBNETB}"
          echo "Found VPC: $${VPC}"

          set_octopusvariable "Role" "$${EXISTINGROLE}"
          set_octopusvariable "SecurityGroup" "$${SECURITYGROUP}"
          set_octopusvariable "SubnetA" "$${SUBNETA}"
          set_octopusvariable "SubnetB" "$${SUBNETB}"
          set_octopusvariable "Vpc" "$${VPC}"

          if [[ -z $${EXISTINGROLE} || -z $${SECURITYGROUP} || -z $${SUBNETA} || -z $${SUBNETB} ]]; then
            echo "[AppBuilder-Infrastructure-ECSResourceLookupFailed](https://github.com/OctopusSamples/content-team-apps/wiki/Error-Codes#appbuilder-infrastructure-ecsresourcelookupfailed) Failed to find one of the resources created with the ECS cluster."
            exit 1
          fi
        EOT
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Deploy ECS Service"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Deploy ECS Service"
      notes          = "Deploy the task definition and service via CloudFormation."
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]
      package {
        name                      = local.package_name
        package_id                = var.octopus_docker_image
        feed_id                   = var.octopus_k8s_feed_id
        acquisition_location      = "NotAcquired"
        extract_during_deployment = false
        properties                = {
          "SelectionMode" : "immediate",
          "Purpose" : "DockerImageReference"
        }
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
              DependsOn: TaskDefinitionBackend
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
                  Ref: TaskExecutionRoleBackend
                RequiresCompatibilities:
                  - FARGATE
                NetworkMode: awsvpc
                Volumes: []
                Tags: []
                RuntimePlatform:
                  OperatingSystemFamily: LINUX
            TaskExecutionRoleBackend:
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
          # You can attach some policies here. Just add AmazonECSTaskExecutionRolePolicyArn as a parameter.
          #      ManagedPolicyArns:
          #        - Ref: AmazonECSTaskExecutionRolePolicyArn
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
            SubnetA:
              Type: String
            SubnetB:
              Type: String
            SecurityGroup:
              Type: String
            EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : "[{\"ParameterKey\":\"ClusterName\",\"ParameterValue\":\"app-builder\"},{\"ParameterKey\":\"TaskDefinitionName\",\"ParameterValue\":\"backend\"},{\"ParameterKey\":\"TaskDefinitionCPU\",\"ParameterValue\":\"256\"},{\"ParameterKey\":\"TaskDefinitionMemory\",\"ParameterValue\":\"512\"},{\"ParameterKey\":\"AmazonECSTaskExecutionRolePolicyArn\",\"ParameterValue\":\"\"},{\"ParameterKey\":\"SubnetA\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetA}\"},{\"ParameterKey\":\"SubnetB\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetB}\"},{\"ParameterKey\":\"SecurityGroup\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SecurityGroup}\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : "[{\"ParameterKey\":\"ClusterName\",\"ParameterValue\":\"app-builder\"},{\"ParameterKey\":\"TaskDefinitionName\",\"ParameterValue\":\"backend\"},{\"ParameterKey\":\"TaskDefinitionCPU\",\"ParameterValue\":\"256\"},{\"ParameterKey\":\"TaskDefinitionMemory\",\"ParameterValue\":\"512\"},{\"ParameterKey\":\"AmazonECSTaskExecutionRolePolicyArn\",\"ParameterValue\":\"\"},{\"ParameterKey\":\"SubnetA\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetA}\"},{\"ParameterKey\":\"SubnetB\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetB}\"},{\"ParameterKey\":\"SecurityGroup\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SecurityGroup}\"}]"
        "Octopus.Action.Aws.IamCapabilities" : "[\"CAPABILITY_AUTO_EXPAND\",\"CAPABILITY_IAM\",\"CAPABILITY_NAMED_IAM\"]"
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
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
      notes                              = "Scans the SBOM for any known vulnerabilities."
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