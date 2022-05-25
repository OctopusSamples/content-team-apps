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
  name                                 = "Backend Service"
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

resource "octopusdeploy_variable" "postman_raw_url_variable" {
  name         = "item:0:request:url:raw"
  type         = "String"
  description  = "A structured variable replacement for the Postman test."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_backend_project.id
  value        = "http://#{Octopus.Action[Find the LoadBalancer URL].Output.DNSName}/api/products/"
}

resource "octopusdeploy_variable" "postman_raw_host_variable" {
  name         = "item:0:request:url:host:0"
  type         = "String"
  description  = "A structured variable replacement for the Postman test."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_backend_project.id
  value        = "#{Octopus.Action[Find the LoadBalancer URL].Output.DNSName}"
}

resource "octopusdeploy_variable" "postman_raw_port_variable" {
  name         = "item:0:request:url:port"
  type         = "String"
  description  = "A structured variable replacement for the Postman test."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_backend_project.id
  value        = "80"
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
        "Octopus.Action.Script.ScriptBody" : local.get_aws_resources
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Backend Service"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Backend Service"
      notes          = "Deploy the task definition, service, target group and listener rule via CloudFormation. The end result is a ECS service exposed by the load balancer created by the ECS Cluster project."
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
            TargetGroup:
              Type: 'AWS::ElasticLoadBalancingV2::TargetGroup'
              Properties:
                TargetGroupAttributes:
                - Key: deregistration_delay.timeout_seconds
                  Value: '20'
                HealthCheckEnabled: true
                HealthCheckIntervalSeconds: 10
                HealthCheckPath: /health/products/GET
                HealthCheckPort: '${local.backend_port}'
                HealthCheckProtocol: HTTP
                HealthCheckTimeoutSeconds: 2
                HealthyThresholdCount: 2
                Matcher:
                  HttpCode: '200'
                Name: OctopubProductsTargetGroup
                Port: ${local.backend_port}
                Protocol: HTTP
                TargetType: ip
                UnhealthyThresholdCount: 5
                VpcId: !Ref Vpc
            ListenerRule:
              Type: 'AWS::ElasticLoadBalancingV2::ListenerRule'
              Properties:
                Actions:
                  - ForwardConfig:
                      TargetGroups:
                        - TargetGroupArn: !Ref TargetGroup
                          Weight: 100
                    Order: 1
                    Type: forward
                Conditions:
                  - Field: path-pattern
                    PathPatternConfig:
                      Values:
                        - /api/products
                        - /api/products/*
                        - /health/products/*
                ListenerArn: !Ref Listener
                Priority: 50
              DependsOn:
                - TargetGroup
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
                LoadBalancers:
                  - ContainerName: backend
                    ContainerPort: ${local.backend_port}
                    TargetGroupArn: !Ref TargetGroup
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
              Default: app-builder-${lower(var.github_repo_owner)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}
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
            Vpc:
              Type: String
            Listener:
              Type: String
          Outputs:
            ServiceName:
              Description: The service name
              Value: !GetAtt
                - ServiceBackend
                - Name
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : "[{\"ParameterKey\":\"Vpc\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.Vpc}\"},{\"ParameterKey\":\"Listener\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.Listener}\"},{\"ParameterKey\":\"TaskDefinitionName\",\"ParameterValue\":\"backend\"},{\"ParameterKey\":\"TaskDefinitionCPU\",\"ParameterValue\":\"256\"},{\"ParameterKey\":\"TaskDefinitionMemory\",\"ParameterValue\":\"512\"},{\"ParameterKey\":\"SubnetA\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetA}\"},{\"ParameterKey\":\"SubnetB\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetB}\"},{\"ParameterKey\":\"SecurityGroup\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SecurityGroup}\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : "[{\"ParameterKey\":\"Vpc\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.Vpc}\"},{\"ParameterKey\":\"Listener\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.Listener}\"},{\"ParameterKey\":\"TaskDefinitionName\",\"ParameterValue\":\"backend\"},{\"ParameterKey\":\"TaskDefinitionCPU\",\"ParameterValue\":\"256\"},{\"ParameterKey\":\"TaskDefinitionMemory\",\"ParameterValue\":\"512\"},{\"ParameterKey\":\"SubnetA\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetA}\"},{\"ParameterKey\":\"SubnetB\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetB}\"},{\"ParameterKey\":\"SecurityGroup\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SecurityGroup}\"}]"
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
    name                = "Find the LoadBalancer URL"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = []
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Find the LoadBalancer URL"
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

          DNSNAME=$(aws cloudformation describe-stacks --stack-name "AppBuilder-ECS-LB-${lower(var.github_repo_owner)}-$${FIXED_ENVIRONMENT}" --query "Stacks[0].Outputs[?OutputKey=='DNSName'].OutputValue" --output text)

          set_octopusvariable "DNSName" "$${DNSNAME}"

          write_highlight "Open [http://$${DNSNAME}/api/products](http://$${DNSNAME}/api/products) to view the backend API."
        EOT
      }
    }
  }
  step {
    condition           = "Success"
    name                = "HTTP Smoke Test"
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
      name                               = "HTTP Smoke Test"
      notes                              = "Use curl to perform a smoke test of a HTTP endpoint."
      environments                       = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]
      script_body = <<-EOT
          # Load balancers can take a minute or so before their DNS is propagated.
          # A status code of 000 means curl could not resolve the DNS name, so we wait for a bit until DNS is updated.
          echo "Waiting for DNS to propagate. This can take a while for a new load balancer."
          for i in {1..60}
          do
              CODE=$(curl -o /dev/null -s -w "%%{http_code}\n" http://#{Octopus.Action[Find the LoadBalancer URL].Output.DNSName}/health/products/GET)
              if [[ "$${CODE}" != "000" ]]
              then
                break
              fi
              echo "Waiting for DNS name to be resolvable"
              sleep 10
          done

          echo "response code: $${CODE}"
          if [ "$${CODE}" == "200" ]
          then
            echo "success"
            exit 0;
          else
            echo "error"
            exit 1;
          fi
        EOT
    }
  }
  step {
    condition           = "Success"
    name                = "Postman Integration Test"
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
      name                               = "Postman Integration Test"
      notes                              = "Use curl to perform a smoke test of a HTTP endpoint."
      environments                       = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]
      features = ["Octopus.Features.JsonConfigurationVariables"]
      container {
        feed_id = var.octopus_k8s_feed_id
        image   = var.postman_docker_image
      }
      package {
        name                      = "products-microservice-postman"
        package_id                = "products-microservice-postman"
        feed_id                   = var.octopus_built_in_feed_id
        acquisition_location      = "Server"
        extract_during_deployment = true
      }
      properties = {
        "Octopus.Action.Package.JsonConfigurationVariablesTargets": "**/*.json"
      }
      script_body = <<-EOT
          echo "##octopus[stdout-verbose]"
          cat products-microservice-postman/test.json
          echo "##octopus[stdout-default]"

          newman run products-microservice-postman/test.json 2>&1
        EOT
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
      script_body = local.vulnerability_scan
    }
  }
}