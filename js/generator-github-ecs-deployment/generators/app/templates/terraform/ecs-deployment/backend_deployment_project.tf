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
  name                                 = "Deploy Backend Service"
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
  backend_package_name = "backend"
  backend_port         = "8083"
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

          # Get the environmen name (or at least up until the first space)
          ENVIRONMENT="#{Octopus.Environment.Name | ToLower}"
          ENVIRONMENT_ARRAY=($ENVIRONMENT)
          FIXED_ENVIRONMENT=$${ENVIRONMENT_ARRAY[0]}

          # ecs-cli creates two public subnets, a VPC, and the VPC security group. We need to find those resources,
          # as we'll place our new ECS services in them.
          SUBNETA=$(aws ec2 describe-subnets --filter "Name=tag:aws:cloudformation:stack-name,Values=amazon-ecs-cli-setup-app-builder-${lower(var.github_repo_owner)}-$${FIXED_ENVIRONMENT}" | jq -r '.Subnets[0].SubnetId')
          SUBNETB=$(aws ec2 describe-subnets --filter "Name=tag:aws:cloudformation:stack-name,Values=amazon-ecs-cli-setup-app-builder-${lower(var.github_repo_owner)}-$${FIXED_ENVIRONMENT}" | jq -r '.Subnets[1].SubnetId')
          VPC=$(aws ec2 describe-vpcs --filter "Name=tag:aws:cloudformation:stack-name,Values=amazon-ecs-cli-setup-app-builder-${lower(var.github_repo_owner)}-$${FIXED_ENVIRONMENT}" | jq -r '.Vpcs[0].VpcId')
          SECURITYGROUP=$(aws ec2 describe-security-groups --filters Name=vpc-id,Values=$${VPC} Name=group-name,Values=default | jq -r '.SecurityGroups[].GroupId')

          # The load balancer listener was created by the infrastructure deployment project, and is read from the CloudFormation stack outputs.
          LISTENER=$(aws cloudformation describe-stacks --stack-name "AppBuilder-ECS-LoadBalancer-${lower(var.github_repo_owner)}-$${FIXED_ENVIRONMENT}" --query "Stacks[0].Outputs[?OutputKey=='Listener'].OutputValue" --output text)

          echo "Found Security Group: $${SECURITYGROUP}"
          echo "Found Subnet A: $${SUBNETA}"
          echo "Found Subnet B: $${SUBNETB}"
          echo "Found VPC: $${VPC}"
          echo "Found Listener: $${LISTENER}"

          set_octopusvariable "SecurityGroup" "$${SECURITYGROUP}"
          set_octopusvariable "SubnetA" "$${SUBNETA}"
          set_octopusvariable "SubnetB" "$${SUBNETB}"
          set_octopusvariable "Vpc" "$${VPC}"
          set_octopusvariable "ClusterName" "app-builder-${lower(var.github_repo_owner)}-$${FIXED_ENVIRONMENT}"
          set_octopusvariable "FixedEnvironment" "$${FIXED_ENVIRONMENT}"
          set_octopusvariable "Listener" $${LISTENER}

          if [[ -z $${SECURITYGROUP} || -z $${SUBNETA} || -z $${SUBNETB} ]]; then
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
        name                      = local.backend_package_name
        package_id                = var.backend_docker_image
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
        "Octopus.Action.Aws.CloudFormationStackName" : "AppBuilder-ECS-Backend-Task-${lower(var.github_repo_owner)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}"
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          # A handy checklist for accessing private ECR repositories:
          # https://stackoverflow.com/a/69643388/157605
          AWSTemplateFormatVersion: '2010-09-09'
          Resources:
            CloudWatchLogsGroup:
              Type: AWS::Logs::LogGroup
              Properties:
                LogGroupName: !Ref AWS::StackName
                RetentionInDays: 14
            ServiceBackend:
              Type: AWS::ECS::Service
              Properties:
                ServiceName: OctopubProducts
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
                    Image: '#{Octopus.Action.Package[${local.backend_package_name}].Image}'
                    Name: backend
                    ResourceRequirements: []
                    Environment:
                      - Name: PORT
                        Value: !!str "${local.backend_port}"
                    EnvironmentFiles: []
                    DisableNetworking: false
                    DnsServers: []
                    DnsSearchDomains: []
                    ExtraHosts: []
                    PortMappings:
                      - ContainerPort: ${local.backend_port}
                        HostPort: ${local.backend_port}
                        Protocol: tcp
                    LogConfiguration:
                      LogDriver: awslogs
                      Options:
                        awslogs-group: !Ref CloudWatchLogsGroup
                        awslogs-region: !Ref AWS::Region
                        awslogs-stream-prefix: backend
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
                Policies:
                  # This is a copy of the AmazonECSTaskExecutionRolePolicy granting access to ECR and CloudWatch
                  # https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task_execution_IAM_role.html
                  - PolicyName: ecrcloudwatch
                    PolicyDocument:
                      Version: "2012-10-17"
                      Statement:
                        - Effect: Allow
                          Action:
                            - ecr:GetAuthorizationToken
                            - ecr:BatchCheckLayerAvailability
                            - ecr:GetDownloadUrlForLayer
                            - ecr:BatchGetImage
                            - logs:CreateLogStream
                            - logs:PutLogEvents
                          Resource: "*"
                Path: /
          Parameters:
            ClusterName:
              Type: String
              Default: app-builder-${lower(var.github_repo_owner)}
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
          Outputs:
            ServiceName:
              Description: The service name
              Value: !GetAtt
                - ServiceBackend
                - Name
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : "[{\"ParameterKey\":\"ClusterName\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.ClusterName}\"},{\"ParameterKey\":\"TaskDefinitionName\",\"ParameterValue\":\"backend\"},{\"ParameterKey\":\"TaskDefinitionCPU\",\"ParameterValue\":\"256\"},{\"ParameterKey\":\"TaskDefinitionMemory\",\"ParameterValue\":\"512\"},{\"ParameterKey\":\"SubnetA\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetA}\"},{\"ParameterKey\":\"SubnetB\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetB}\"},{\"ParameterKey\":\"SecurityGroup\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SecurityGroup}\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : "[{\"ParameterKey\":\"ClusterName\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.ClusterName}\"},{\"ParameterKey\":\"TaskDefinitionName\",\"ParameterValue\":\"backend\"},{\"ParameterKey\":\"TaskDefinitionCPU\",\"ParameterValue\":\"256\"},{\"ParameterKey\":\"TaskDefinitionMemory\",\"ParameterValue\":\"512\"},{\"ParameterKey\":\"SubnetA\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetA}\"},{\"ParameterKey\":\"SubnetB\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetB}\"},{\"ParameterKey\":\"SecurityGroup\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SecurityGroup}\"}]"
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
    name                = "Find the Task IP Address"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = []
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Find the Task IP Address"
      notes          = "Queries the task for the public IP address."
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]
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

          # Get the environmen name (or at least up until the first space)
          ENVIRONMENT="#{Octopus.Environment.Name | ToLower}"
          ENVIRONMENT_ARRAY=($ENVIRONMENT)
          FIXED_ENVIRONMENT=$${ENVIRONMENT_ARRAY[0]}

          # Get the first task on the cluster
          TASKS=$(aws ecs list-tasks --cluster app-builder-mcasperson-development | jq -r '[.taskArns[]]| join(" ")')
          TASK=$(aws ecs describe-tasks --cluster app-builder-${lower(var.github_repo_owner)}-$${FIXED_ENVIRONMENT} --tasks $${TASKS} | jq -r '.tasks[] | select(.group | startswith("service:OctopubProducts")) | .taskArn')
          echo "Found Task $${TASK}"

          if [[ "$${TASK}" == "null" || -z "$${TASK}" ]]; then
            echo "Unable to find the task"
            exit 0
          fi

          # Get the network interface
          ENI=$(aws ecs describe-tasks --cluster app-builder-${lower(var.github_repo_owner)}-$${FIXED_ENVIRONMENT} --tasks $${TASK} | jq -r '.tasks[0].attachments[].details[] | select(.name == "networkInterfaceId") | .value')
          echo "Found Elastic Network Interface $${ENI}"

          if [[ "$${ENI}" == "null" || -z "$${ENI}" ]]; then
            echo "Unable to find the ENI"
            exit 0
          fi

          # Get the public IP
          IP=$(aws ec2 describe-network-interfaces --network-interface-ids $${ENI} | jq -r '.NetworkInterfaces[0].Association.PublicIp')

          if [[ "$${IP}" == "null" || -z "$${IP}" ]]; then
            echo "Unable to find the IP"
            exit 0
          fi

          echo "Open [http://$${IP}:8083/api/products](http://$${IP}:8083/api/products) to view the backend API."
        EOT
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
        name                      = "products-microservice-sbom"
        package_id                = "products-microservice-sbom"
        feed_id                   = var.octopus_built_in_feed_id
        acquisition_location      = "Server"
        extract_during_deployment = true
      }
      script_body = <<-EOT
          echo "##octopus[stdout-verbose]"
          docker pull appthreat/dep-scan
          echo "##octopus[stdout-default]"


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