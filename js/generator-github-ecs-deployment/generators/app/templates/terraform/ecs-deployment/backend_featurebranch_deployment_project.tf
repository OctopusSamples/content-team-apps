resource "octopusdeploy_project" "deploy_backend_featurebranch_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys the backend feature branch service to ECS."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_application_lifecycle_id
  name                                 = "Backend Feature Branch Service"
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

resource "octopusdeploy_variable" "debug_featurebranch_variable" {
  name         = "OctopusPrintVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_backend_featurebranch_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "debug_featurebranch_evaluated_variable" {
  name         = "OctopusPrintEvaluatedVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_backend_featurebranch_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "aws_account_deploy_backend_featurebranch_project" {
  name     = "AWS Account"
  type     = "AmazonWebServicesAccount"
  value    = var.octopus_aws_account_id
  owner_id = octopusdeploy_project.deploy_backend_featurebranch_project.id
}

resource "octopusdeploy_variable" "postman_raw_url_featurebranch_variable" {
  name         = "item:0:request:url:raw"
  type         = "String"
  description  = "A structured variable replacement for the Postman test."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_backend_featurebranch_project.id
  value        = "http://#{Octopus.Action[Backend Service].Output.AwsOutputs[DNSName]}/api/products/"
}

resource "octopusdeploy_variable" "postman_raw_host_featurebranch_variable" {
  name         = "item:0:request:url:host:0"
  type         = "String"
  description  = "A structured variable replacement for the Postman test."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_backend_featurebranch_project.id
  value        = "#{Octopus.Action[Backend Service].Output.AwsOutputs[DNSName]}"
}

resource "octopusdeploy_variable" "postman_raw_port_featurebranch_variable" {
  name         = "item:0:request:url:port"
  type         = "String"
  description  = "A structured variable replacement for the Postman test."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_backend_featurebranch_project.id
  value        = "80"
}

locals {
  backend_dns_branch_name = "#{Octopus.Action[Backend Service].Package[${local.backend_package_name}].PackageVersion | VersionPreRelease | Replace \"\\..*\" \"\" | ToLower}"
  backend_trimmed_dns_branch_name = "#{Octopus.Action[Backend Service].Package[${local.backend_package_name}].PackageVersion | VersionPreRelease | Replace \"\\..*\" \"\" | ToLower | Substring 10}"
  # This needs to be under 32 characters, and yet still unique per user / environment / branch. We trim a few strings to try and keep it under the limit.
  backend_featurebranch_loadbalancer_name = "ECS-PD-${substr(lower(var.github_repo_owner), 0, 10)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment | Substring 3}-${local.backend_trimmed_dns_branch_name}"
  backend_featurebranch_targetgroup_name = "ECS-PD-${substr(lower(var.github_repo_owner), 0, 10)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment | Substring 3}-${local.backend_trimmed_dns_branch_name}"
}

resource "octopusdeploy_deployment_process" "deploy_backend_featurebranch" {
  project_id = octopusdeploy_project.deploy_backend_featurebranch_project.id
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
      notes          = "Deploy the task definition, service, target group, listener, and load balancer rule via CloudFormation. The end result is a ECS service exposed by it's own unique load balancer."
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
        "Octopus.Action.Aws.CloudFormationStackName" : "AppBuilder-ECS-Backend-Task-${lower(var.github_repo_owner)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}-${local.backend_dns_branch_name}"
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          # A handy checklist for accessing private ECR repositories:
          # https://stackoverflow.com/a/69643388/157605
          AWSTemplateFormatVersion: '2010-09-09'
          Resources:
            # The load balancer security group allows HTTP and HTTPS traffic.
            ALBSecurityGroup:
              Type: "AWS::EC2::SecurityGroup"
              Properties:
                GroupDescription: 'ALB Security group #{Octopus.Action[Get AWS Resources].Output.FixedEnvironment} ${local.backend_dns_branch_name}'
                GroupName: 'octopub-prd-alb-sg-${lower(var.github_repo_owner)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}-${local.backend_dns_branch_name}'
                Tags:
                  - Key: "Name"
                    Value: 'octopub-prd-alb-sg-${lower(var.github_repo_owner)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}-${local.backend_dns_branch_name}'
                VpcId: !Ref Vpc
                SecurityGroupIngress:
                  - CidrIp: "0.0.0.0/0"
                    FromPort: 80
                    IpProtocol: "tcp"
                    ToPort: 80
                  - CidrIp: "0.0.0.0/0"
                    FromPort: 443
                    IpProtocol: "tcp"
                    ToPort: 443
            # The ECS service exposes the container port.
            BackendSecurityGroup:
              Type: "AWS::EC2::SecurityGroup"
              Properties:
                GroupDescription: 'Product Security group #{Octopus.Action[Get AWS Resources].Output.FixedEnvironment} ${local.backend_dns_branch_name}'
                GroupName: 'octopub-prd-sg-${lower(var.github_repo_owner)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}-${local.backend_dns_branch_name}'
                Tags:
                  - Key: "Name"
                    Value: 'octopub-prd-sg-${lower(var.github_repo_owner)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}-${local.backend_dns_branch_name}'
                VpcId: !Ref Vpc
                SecurityGroupIngress:
                  - CidrIp: "0.0.0.0/0"
                    FromPort: ${local.backend_port}
                    IpProtocol: "tcp"
                    ToPort: ${local.backend_port}
            # Each feature branch is exposed on its own load balancer.
            ApplicationLoadBalancer:
              Type: "AWS::ElasticLoadBalancingV2::LoadBalancer"
              Properties:
                Name: '${local.backend_featurebranch_loadbalancer_name}'
                Scheme: "internet-facing"
                Type: "application"
                Subnets:
                  - !Ref SubnetA
                  - !Ref SubnetB
                SecurityGroups:
                  - !Ref ALBSecurityGroup
                IpAddressType: "ipv4"
                LoadBalancerAttributes:
                  - Key: "access_logs.s3.enabled"
                    Value: "false"
                  - Key: "idle_timeout.timeout_seconds"
                    Value: "60"
                  - Key: "deletion_protection.enabled"
                    Value: "false"
                  - Key: "routing.http2.enabled"
                    Value: "true"
                  - Key: "routing.http.drop_invalid_header_fields.enabled"
                    Value: "false"
            # The listener defines the traffic accecpted by the load balancer, which in this case is HTTP traffic.
            # It also has a default rule to return 404 if no other listener rules match.
            Listener:
              Type: 'AWS::ElasticLoadBalancingV2::Listener'
              Properties:
                DefaultActions:
                  - FixedResponseConfig:
                      StatusCode: '404'
                    Order: 1
                    Type: fixed-response
                LoadBalancerArn: !Ref ApplicationLoadBalancer
                Port: 80
                Protocol: HTTP
            # Target groups reference the ECS services. The services always have a random IP address, so a target group
            # provides a way to expose the IP addresses and load balance between multiple services.
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
                Name: '${local.backend_featurebranch_targetgroup_name}'
                Port: ${local.backend_port}
                Protocol: HTTP
                TargetType: ip
                UnhealthyThresholdCount: 5
                VpcId: !Ref Vpc
            # The listener rule defines how traffic is forwarded from the listener to the target group, and in trun
            # to the ECS services.
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
            # This the the log group for the ECS service.
            CloudWatchLogsGroup:
              Type: AWS::Logs::LogGroup
              Properties:
                LogGroupName: !Ref AWS::StackName
                RetentionInDays: 14
            # This is the ECS service, which registers itself with the target group defined above.
            # Note that we need to depend on the listener rule and listener, which ensures the service is only
            # created once it can be assigned to a load balancer. Without these dependencies, CloudFormation may fail to
            # deploy the template, as ECS requires services have an active load balancer.
            ServiceBackend:
              Type: AWS::ECS::Service
              Properties:
                ServiceName: 'OctopubProducts-${lower(var.github_repo_owner)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}-${local.backend_dns_branch_name}'
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
                      - !Ref BackendSecurityGroup
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
              DependsOn:
                - TaskDefinitionBackend
                - ListenerRule
                - Listener
            # The task definition defines the images used to build the containers managed by the ECS service.
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
                Family: !Sub '$${TaskDefinitionName}-${local.backend_dns_branch_name}'
                Cpu: !Ref TaskDefinitionCPU
                Memory: !Ref TaskDefinitionMemory
                ExecutionRoleArn: !Ref TaskExecutionRoleBackend
                RequiresCompatibilities:
                  - FARGATE
                NetworkMode: awsvpc
                Volumes: []
                Tags: []
                RuntimePlatform:
                  OperatingSystemFamily: LINUX
            # This role allows the ECS service to download images from ECR and create log entries.
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
              Default: 'app-builder-${var.github_repo_owner}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}'
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
            Vpc:
              Type: String
          Outputs:
            ServiceName:
              Description: The service name
              Value: !GetAtt
                - ServiceBackend
                - Name
            DNSName:
              Description: The listener
              Value: !GetAtt
              - ApplicationLoadBalancer
              - DNSName
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : "[{\"ParameterKey\":\"Vpc\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.Vpc}\"},{\"ParameterKey\":\"TaskDefinitionName\",\"ParameterValue\":\"backend\"},{\"ParameterKey\":\"TaskDefinitionCPU\",\"ParameterValue\":\"256\"},{\"ParameterKey\":\"TaskDefinitionMemory\",\"ParameterValue\":\"512\"},{\"ParameterKey\":\"SubnetA\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetA}\"},{\"ParameterKey\":\"SubnetB\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetB}\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : "[{\"ParameterKey\":\"Vpc\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.Vpc}\"},{\"ParameterKey\":\"TaskDefinitionName\",\"ParameterValue\":\"backend\"},{\"ParameterKey\":\"TaskDefinitionCPU\",\"ParameterValue\":\"256\"},{\"ParameterKey\":\"TaskDefinitionMemory\",\"ParameterValue\":\"512\"},{\"ParameterKey\":\"SubnetA\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetA}\"},{\"ParameterKey\":\"SubnetB\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetB}\"}]"
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
          write_highlight "Open [http://#{Octopus.Action[Backend Service].Output.AwsOutputs[DNSName]}/api/products](http://#{Octopus.Action[Backend Service].Output.AwsOutputs[DNSName]}/api/products) to view the backend API."
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
          for i in {1..30}
          do
              CODE=$(curl -o /dev/null -s -w "%%{http_code}\n" http://#{Octopus.Action[Backend Service].Output.AwsOutputs[DNSName]}/health/products/GET)
              if [[ "$${CODE}" == "200" ]]
              then
                break
              fi
              echo "Waiting for DNS name to be resolvable and for service to respond"
              sleep 10
          done

          echo "response code: $${CODE}"
          if [[ "$${CODE}" == "200" ]]
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