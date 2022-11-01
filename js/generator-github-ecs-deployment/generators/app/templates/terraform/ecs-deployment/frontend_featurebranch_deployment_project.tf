resource "octopusdeploy_project" "deploy_frontend_featurebranch_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys a frontend webapp feature branch to ECS."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_application_lifecycle_id
  name                                 = "Frontend Feature Branch WebApp"
  project_group_id                     = octopusdeploy_project_group.frontend_project_group.id
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

resource "octopusdeploy_variable" "frontend_featurebranch_debug_variable" {
  name         = "OctopusPrintVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_frontend_featurebranch_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "frontend_featurebranch_debug_evaluated_variable" {
  name         = "OctopusPrintEvaluatedVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_frontend_featurebranch_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "aws_account_deploy_frontend_featurebranch_project" {
  name     = "AWS Account"
  type     = "AmazonWebServicesAccount"
  value    = var.octopus_aws_account_id
  owner_id = octopusdeploy_project.deploy_frontend_featurebranch_project.id
}

resource "octopusdeploy_variable" "cypress_baseurl_variable_featurebranch" {
  name         = "baseUrl"
  type         = "String"
  description  = "A structured variable replacement for the Cypress test."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_frontend_featurebranch_project.id
  value        = "http://#{Octopus.Action[Frontend WebApp].Output.AwsOutputs[DNSName]}"
}


locals {
  frontend_proxy_package_name = "proxy"
  # This is the package semver prerelease label up until the first period, so a version of "1.0.0-MYBranch.1" becomes "mybranch"
  frontend_dns_branch_name = "#{Octopus.Action[Frontend WebApp].Package[${local.frontend_package_id}].PackageVersion | VersionPreRelease | Replace \"\\..*\" \"\" | ToLower}"
  # This is the first 10 characters of the prerelease, used in names that have limited characters
  frontend_trimmed_dns_branch_name = "#{Octopus.Action[Frontend WebApp].Package[${local.frontend_package_id}].PackageVersion | VersionPreRelease | Replace \"\\..*\" \"\" | ToLower | Substring 10}"
  # The stack names can be 128 chars long
  frontend_cf_stack_name = "ECS-FE-${lower(var.github_repo_owner)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}-${local.frontend_dns_branch_name}"
  # This needs to be under 32 characters, and yet still unique per user / environment / branch. We trim a few strings to try and keep it under the limit.
  frontend_featurebranch_target_group_name = "ECS-FE-${substr(lower(var.github_repo_owner), 0, 10)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment | Substring 3}-${local.frontend_trimmed_dns_branch_name}"
  frontend_featurebranch_proxy_target_group_name = "ECS-PX-${substr(lower(var.github_repo_owner), 0, 10)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment | Substring 3}-${local.frontend_trimmed_dns_branch_name}"
  frontend_featurebranch_loadbalancer_name = "ECS-LB-${substr(lower(var.github_repo_owner), 0, 10)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment | Substring 3}-${local.frontend_trimmed_dns_branch_name}"
  frontend_featurebranch_service_name = "Web-${substr(lower(var.github_repo_owner), 0, 10)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment | Substring 3}-${local.frontend_trimmed_dns_branch_name}"
  frontend_featurebranch_proxy_service_name = "WebPxy-${substr(lower(var.github_repo_owner), 0, 10)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment | Substring 3}-${local.frontend_trimmed_dns_branch_name}"
}

resource "octopusdeploy_deployment_process" "deploy_frontend_featurebranch" {
  project_id = octopusdeploy_project.deploy_frontend_featurebranch_project.id
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
    name                = "Frontend WebApp"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Frontend WebApp"
      notes          = "Deploy the task definition, service, target group, listener rule, and load balancer via CloudFormation. The end result is a ECS service exposed by its own unique load balancer."
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]
      package {
        name                      = local.frontend_package_id
        package_id                = var.frontend_docker_image
        feed_id                   = var.octopus_k8s_feed_id
        acquisition_location      = "NotAcquired"
        extract_during_deployment = false
        properties                = {
          "SelectionMode" : "immediate",
          "Purpose" : "DockerImageReference"
        }
      }
      package {
        name                      = local.frontend_proxy_package_name
        package_id                = "octopussamples/dumb-reverse-proxy"
        feed_id                   = var.octopus_dockerhub_feed_id
        acquisition_location      = "NotAcquired"
        extract_during_deployment = false
      }

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : "[]"
        "Octopus.Action.Aws.CloudFormationStackName" : local.frontend_cf_stack_name
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          # A handy checklist for accessing private ECR repositories:
          # https://stackoverflow.com/a/69643388/157605

          # ALBs don't have the ability to rewrite paths, which means feature branch instances are not easily deployed
          # under their own subpath on a shared load balancer. The individual services being deployed could, in theory,
          # be configured to respond to requests under a new subpath, but this functionality is not universal. For example,
          # the Docker image created to host a static web app using the Heroku NGINX buildpack doesn't expose the ability
          # to change the root directory based on an easily configurable external value like an environment variable.
          #
          # To demonstrate a more universal approach, each feature branch is exposed via its own load balancer. This means
          # each feature branch uses the same path structure as the main instance. Where access to other services are required,
          # a dumb reverse proxy forwards the request to the main load balancer.
          AWSTemplateFormatVersion: '2010-09-09'
          Resources:
            ALBSecurityGroup:
              Type: "AWS::EC2::SecurityGroup"
              Properties:
                GroupDescription: "ALB Security group #{Octopus.Action[Get AWS Resources].Output.FixedEnvironment} ${local.frontend_dns_branch_name}"
                GroupName: "octopub-fe-alb-sg-${lower(var.github_repo_owner)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}-${local.frontend_dns_branch_name}"
                Tags:
                  - Key: "Name"
                    Value: "octopub-fe-alb-sg-${lower(var.github_repo_owner)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}-${local.frontend_dns_branch_name}"
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
            FrontendSecurityGroup:
              Type: "AWS::EC2::SecurityGroup"
              Properties:
                GroupDescription: "Frontend Security group #{Octopus.Action[Get AWS Resources].Output.FixedEnvironment} ${local.frontend_dns_branch_name}"
                GroupName: "octopub-fe-sg-${lower(var.github_repo_owner)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}-${local.frontend_dns_branch_name}"
                Tags:
                  - Key: "Name"
                    Value: "octopub-fe-sg-${lower(var.github_repo_owner)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}-${local.frontend_dns_branch_name}"
                VpcId: !Ref Vpc
                SecurityGroupIngress:
                  - CidrIp: "0.0.0.0/0"
                    FromPort: 5000
                    IpProtocol: "tcp"
                    ToPort: 5000
            BackendProxySecurityGroup:
              Type: "AWS::EC2::SecurityGroup"
              Properties:
                GroupDescription: "Backend Proxy Security group #{Octopus.Action[Get AWS Resources].Output.FixedEnvironment} ${local.frontend_dns_branch_name}"
                GroupName: "octopub-prx-sg-${lower(var.github_repo_owner)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}-${local.frontend_dns_branch_name}"
                Tags:
                  - Key: "Name"
                    Value: "octopub-prx-sg-${lower(var.github_repo_owner)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}-${local.frontend_dns_branch_name}"
                VpcId: !Ref Vpc
                SecurityGroupIngress:
                  - CidrIp: "0.0.0.0/0"
                    FromPort: 8080
                    IpProtocol: "tcp"
                    ToPort: 8080
                  - CidrIp: "0.0.0.0/0"
                    FromPort: 8081
                    IpProtocol: "tcp"
                    ToPort: 8081
            ApplicationLoadBalancer:
              Type: "AWS::ElasticLoadBalancingV2::LoadBalancer"
              Properties:
                Name: '${local.frontend_featurebranch_loadbalancer_name}'
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
            TargetGroup:
              Type: 'AWS::ElasticLoadBalancingV2::TargetGroup'
              Properties:
                TargetGroupAttributes:
                - Key: deregistration_delay.timeout_seconds
                  Value: '20'
                HealthCheckEnabled: true
                HealthCheckIntervalSeconds: 5
                HealthCheckPath: /
                HealthCheckPort: '${local.frontend_port}'
                HealthCheckProtocol: HTTP
                HealthCheckTimeoutSeconds: 2
                HealthyThresholdCount: 2
                Matcher:
                  HttpCode: '200'
                Name: ${local.frontend_featurebranch_target_group_name}
                Port: ${local.frontend_port}
                Protocol: HTTP
                TargetType: ip
                UnhealthyThresholdCount: 5
                VpcId: !Ref Vpc
            ProxyTargetGroup:
              Type: 'AWS::ElasticLoadBalancingV2::TargetGroup'
              Properties:
                TargetGroupAttributes:
                - Key: deregistration_delay.timeout_seconds
                  Value: '20'
                HealthCheckEnabled: true
                HealthCheckIntervalSeconds: 5
                HealthCheckPath: /
                HealthCheckPort: 8081
                HealthCheckProtocol: HTTP
                HealthCheckTimeoutSeconds: 2
                HealthyThresholdCount: 2
                Matcher:
                  HttpCode: '200'
                Name: ${local.frontend_featurebranch_proxy_target_group_name}
                Port: 8080
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
                        - /*
                ListenerArn: !Ref Listener
                Priority: 200
              DependsOn:
                - TargetGroup
            ProxyListenerRule:
              Type: 'AWS::ElasticLoadBalancingV2::ListenerRule'
              Properties:
                Actions:
                  - ForwardConfig:
                      TargetGroups:
                        - TargetGroupArn: !Ref ProxyTargetGroup
                          Weight: 100
                    Order: 1
                    Type: forward
                Conditions:
                  - Field: path-pattern
                    PathPatternConfig:
                      Values:
                        - /api/*
                        - /health/*
                ListenerArn: !Ref Listener
                Priority: 100
              DependsOn:
                - ProxyTargetGroup
            CloudWatchLogsGroup:
              Type: AWS::Logs::LogGroup
              Properties:
                LogGroupName: !Ref AWS::StackName
                RetentionInDays: 14
            ServiceFrontend:
              Type: AWS::ECS::Service
              Properties:
                ServiceName: ${local.frontend_featurebranch_service_name}
                Cluster: !Ref ClusterName
                TaskDefinition: !Ref TaskDefinitionFrontend
                DesiredCount: 1
                EnableECSManagedTags: false
                Tags: []
                LaunchType: FARGATE
                NetworkConfiguration:
                  AwsvpcConfiguration:
                    AssignPublicIp: ENABLED
                    SecurityGroups:
                      - !Ref FrontendSecurityGroup
                    Subnets:
                      - !Ref SubnetA
                      - !Ref SubnetB
                LoadBalancers:
                  - ContainerName: frontend
                    ContainerPort: ${local.frontend_port}
                    TargetGroupArn: !Ref TargetGroup
                DeploymentConfiguration:
                  MaximumPercent: 200
                  MinimumHealthyPercent: 100
              DependsOn:
                - TaskDefinitionFrontend
                - Listener
                - ListenerRule
                - TargetGroup
            ServiceBackendProxy:
              Type: AWS::ECS::Service
              Properties:
                ServiceName: ${local.frontend_featurebranch_proxy_service_name}
                Cluster: !Ref ClusterName
                TaskDefinition: !Ref TaskDefinitionProxy
                DesiredCount: 1
                EnableECSManagedTags: false
                Tags: []
                LaunchType: FARGATE
                NetworkConfiguration:
                  AwsvpcConfiguration:
                    AssignPublicIp: ENABLED
                    SecurityGroups:
                      - !Ref BackendProxySecurityGroup
                    Subnets:
                      - !Ref SubnetA
                      - !Ref SubnetB
                LoadBalancers:
                  - ContainerName: proxy
                    ContainerPort: 8080
                    TargetGroupArn: !Ref ProxyTargetGroup
                DeploymentConfiguration:
                  MaximumPercent: 200
                  MinimumHealthyPercent: 100
              DependsOn:
                - TaskDefinitionProxy
                - Listener
                - ProxyListenerRule
                - ProxyTargetGroup
            TaskDefinitionFrontend:
              Type: AWS::ECS::TaskDefinition
              Properties:
                ContainerDefinitions:
                  - Essential: true
                    Image: '#{Octopus.Action.Package[${local.frontend_package_id}].Image}'
                    Name: frontend
                    ResourceRequirements: []
                    Environment:
                      - Name: PORT
                        Value: !!str "${local.frontend_port}"
                    EnvironmentFiles: []
                    DisableNetworking: !!bool false
                    DnsServers: []
                    DnsSearchDomains: []
                    ExtraHosts: []
                    PortMappings:
                      - ContainerPort: !!int ${local.frontend_port}
                        HostPort: !!int ${local.frontend_port}
                        Protocol: tcp
                    LogConfiguration:
                      LogDriver: awslogs
                      Options:
                        awslogs-group: !Ref CloudWatchLogsGroup
                        awslogs-region: !Ref AWS::Region
                        awslogs-stream-prefix: frontend-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}
                Family: !Sub $${TaskDefinitionName}-${local.frontend_dns_branch_name}
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
            TaskDefinitionProxy:
              Type: AWS::ECS::TaskDefinition
              Properties:
                ContainerDefinitions:
                  - Essential: !!bool true
                    Image: "#{Octopus.Action.Package[${local.frontend_proxy_package_name}].Image}"
                    Name: proxy
                    ResourceRequirements: []
                    Environment:
                      - Name: DEFAULT_URL
                        Value: !Sub "http://$${MainLoadBalancer}"
                      - Name: COGNITO_DISABLE_AUTH
                        Value: !!str "true"
                    EnvironmentFiles: []
                    DisableNetworking: !!bool false
                    DnsServers: []
                    DnsSearchDomains: []
                    ExtraHosts: []
                    PortMappings:
                      - ContainerPort: !!int 8080
                        HostPort: !!int 8080
                        Protocol: tcp
                    LogConfiguration:
                      LogDriver: awslogs
                      Options:
                        awslogs-group: !Ref CloudWatchLogsGroup
                        awslogs-region: !Ref AWS::Region
                        awslogs-stream-prefix: frontend-proxy-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}
                Family: !Sub $${TaskDefinitionName}-${local.frontend_dns_branch_name}-proxy
                Cpu: 256
                Memory: 512
                ExecutionRoleArn: !Ref TaskExecutionRoleBackend
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
              Default: app-builder-${var.github_repo_owner}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}
            TaskDefinitionName:
              Type: String
              Default: frontend
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
            MainLoadBalancer:
              Type: String
          Outputs:
            ServiceName:
              Description: The service name
              Value: !GetAtt
                - ServiceFrontend
                - Name
            ServiceProxyName:
              Description: The proxy service name
              Value: !GetAtt
                - ServiceBackendProxy
                - Name
            DNSName:
              Description: The listener
              Value: !GetAtt
              - ApplicationLoadBalancer
              - DNSName
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : "[{\"ParameterKey\":\"MainLoadBalancer\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.DNSName}\"},{\"ParameterKey\":\"Vpc\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.Vpc}\"},{\"ParameterKey\":\"TaskDefinitionName\",\"ParameterValue\":\"frontend\"},{\"ParameterKey\":\"TaskDefinitionCPU\",\"ParameterValue\":\"256\"},{\"ParameterKey\":\"TaskDefinitionMemory\",\"ParameterValue\":\"512\"},{\"ParameterKey\":\"SubnetA\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetA}\"},{\"ParameterKey\":\"SubnetB\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetB}\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : "[{\"ParameterKey\":\"MainLoadBalancer\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.DNSName}\"},{\"ParameterKey\":\"Vpc\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.Vpc}\"},{\"ParameterKey\":\"TaskDefinitionName\",\"ParameterValue\":\"frontend\"},{\"ParameterKey\":\"TaskDefinitionCPU\",\"ParameterValue\":\"256\"},{\"ParameterKey\":\"TaskDefinitionMemory\",\"ParameterValue\":\"512\"},{\"ParameterKey\":\"SubnetA\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetA}\"},{\"ParameterKey\":\"SubnetB\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetB}\"}]"
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
          write_highlight "Open [http://#{Octopus.Action[Frontend WebApp].Output.AwsOutputs[DNSName]}/index.html](http://#{Octopus.Action[Frontend WebApp].Output.AwsOutputs[DNSName]}/index.html) to view the frontend webapp."
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
              CODE=$(curl -o /dev/null -s -w "%%{http_code}\n" http://#{Octopus.Action[Frontend WebApp].Output.AwsOutputs[DNSName]}/index.html)
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
    name                = "Cypress E2E Test"
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
      name                               = "Cypress E2E Test"
      notes                              = "Use cypress to perform an end to end test of the frontend web app."
      environments                       = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]
      features = ["Octopus.Features.JsonConfigurationVariables"]
      container {
        feed_id = var.octopus_k8s_feed_id
        image   = var.cypress_docker_image
      }
      package {
        name                      = "octopub-frontend-cypress"
        package_id                = "octopub-frontend-cypress"
        feed_id                   = var.octopus_built_in_feed_id
        acquisition_location      = "Server"
        extract_during_deployment = true
      }
      properties = {
        "Octopus.Action.Package.JsonConfigurationVariablesTargets": "**/cypress.json"
      }
      script_body = <<-EOT
          echo "##octopus[stdout-verbose]"
          cd octopub-frontend-cypress
          OUTPUT=$(cypress run 2>&1)
          RESULT=$?
          echo "##octopus[stdout-default]"

          # Print the output stripped of ANSI colour codes
          echo -e "$${OUTPUT}" | sed 's/\x1b\[[0-9;]*m//g'

          if [[ -f mochawesome.html ]]
          then
            inline-assets mochawesome.html selfcontained.html
            new_octopusartifact "$${PWD}/selfcontained.html" "html-report.html"
          fi
          if [[ -d cypress/screenshots/sample_spec.js ]]
          then
            zip -r screenshots.zip cypress/screenshots/sample_spec.js
            new_octopusartifact "$${PWD}/screenshots.zip" "screenshots.zip"
          fi
          exit $${RESULT}
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
        name                      = "javascript-frontend-sbom"
        package_id                = "javascript-frontend-sbom"
        feed_id                   = var.octopus_built_in_feed_id
        acquisition_location      = "Server"
        extract_during_deployment = true
      }
      script_body = local.vulnerability_scan
    }
  }
}