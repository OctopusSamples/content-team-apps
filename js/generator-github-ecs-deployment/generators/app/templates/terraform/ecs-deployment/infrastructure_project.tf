resource "octopusdeploy_project" "deploy_infrastructure_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys the ECS cluster using ecs-cli. This project is created and managed by the [Octopus Terraform provider](https://registry.terraform.io/providers/OctopusDeployLabs/octopusdeploy/latest/docs). The Terraform files can be found in the [GitHub repo](https://github.com/${var.github_repo})."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_infrastructure_lifecycle_id
  name                                 = "ECS Cluster"
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

resource "octopusdeploy_deployment_process" "deploy_cluster" {
  project_id = octopusdeploy_project.deploy_infrastructure_project.id
  step {
    condition           = "Success"
    name                = "Create an ECS cluster"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = []
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Create an ECS Cluster"
      notes          = "Create an ECS cluster with ecs-cli"
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
        "Octopus.Action.Script.ScriptBody": <<-EOT
          # Get the containers used as CLI tools.
          # Docker provides a useful, and (nearly) universal package manager for CLI tooling. Images are downloaded
          # and cached as a usual part of the Docker workflow, providing us with a performant solution that reduces
          # the need to redownload images when reusing workers. It also means we don't have to worry about modifying
          # workers in the same way that we would if we needed to download raw executables and save them in the shared
          # file system.
          echo "Downloading Docker images"
          echo "##octopus[stdout-verbose]"
          docker pull amazon/aws-cli 2>&1
          docker pull imega/jq 2>&1
          echo "##octopus[stdout-default]"

          # Install the ecsctl tool. Unfortunately there is no officially maintained or third party Docker image for
          # this tool.
          if [[ ! -f /usr/local/bin/ecs-cli ]]; then
              echo "Installing the ecs-cli tool"
              echo "##octopus[stdout-verbose]"
              curl --silent -Lo ecs-cli https://amazon-ecs-cli.s3.amazonaws.com/ecs-cli-linux-amd64-latest
              chmod +x ecs-cli
          fi
          echo "##octopus[stdout-default]"

          # Alias the docker run commands. This allows us to run the Docker images like regular CLI commands.
          shopt -s expand_aliases
          alias aws="docker run --rm -i -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY amazon/aws-cli"
          alias jq="docker run --rm -i imega/jq"

          # Get the environmen name (or at least up until the first space)
          ENVIRONMENT="#{Octopus.Environment.Name | ToLower}"
          ENVIRONMENT_ARRAY=($ENVIRONMENT)
          FIXED_ENVIRONMENT=$${ENVIRONMENT_ARRAY[0]}

          # Create the cluster using the instructions from https://docs.aws.amazon.com/AmazonECS/latest/developerguide/ecs-cli-tutorial-fargate.html
          # Find any existing cluster with the name "app-builder".
          EXISTINGCLUSTER=$(aws ecs list-clusters | jq -r ".clusterArns[] | select(. | endswith(\"/app-builder-${var.github_repo_owner}-$${FIXED_ENVIRONMENT}\"))")

          # If the cluster does not exist, create it.
          if [[ -z "$${EXISTINGCLUSTER}" ]]; then
            echo "Creating ECS cluster"
            echo "##octopus[stdout-verbose]"

            ./ecs-cli configure --cluster app-builder-${var.github_repo_owner}-$${FIXED_ENVIRONMENT} --default-launch-type FARGATE --config-name app-builder-${var.github_repo_owner}-$${FIXED_ENVIRONMENT} --region $${AWS_DEFAULT_REGION}
            ./ecs-cli configure profile --access-key $${AWS_ACCESS_KEY_ID} --secret-key $${AWS_SECRET_ACCESS_KEY} --profile-name app-builder-${var.github_repo_owner}-$${FIXED_ENVIRONMENT}-profile
            ./ecs-cli up --cluster-config app-builder-${var.github_repo_owner}-$${FIXED_ENVIRONMENT} --ecs-profile app-builder-${var.github_repo_owner}-$${FIXED_ENVIRONMENT}-profile --tags 'CreatedBy=AppBuilder,TargetType=ECS' > output.txt
            RESULT=$?
            cat output.txt

            if [[ $RESULT -ne 0 ]]; then
              echo "##octopus[stdout-default]"
              write_highlight "[AppBuilder-Infrastructure-ECSFailed](https://github.com/OctopusSamples/content-team-apps/wiki/Error-Codes#appbuilder-infrastructure-ecsfailed) Failed to create the cluster with ecs-cli."
              exit 1
            fi

            # Extract the VPC and subnets from the ecs-cli output text.
            VPC=$(awk '/VPC created:/{print $NF}' output.txt)
            SUBNETS=$(awk '/Subnet created:/{print $NF}' output.txt)

            # Find the security group associated with the VPC.
            SECURITYGROUP=$(aws ec2 describe-security-groups --filters Name=vpc-id,Values=$${VPC} | jq -r '.SecurityGroups[].GroupId')

            # Expose port 8083 and 5000, which is used by the sample applications.
            aws ec2 authorize-security-group-ingress --group-id $${SECURITYGROUP} --protocol tcp --port 8083 --cidr 0.0.0.0/0
            aws ec2 authorize-security-group-ingress --group-id $${SECURITYGROUP} --protocol tcp --port 5000 --cidr 0.0.0.0/0
            echo "##octopus[stdout-default]"
          else
            echo "ECS Cluster already exists with ARN $${EXISTINGCLUSTER}"
          fi

          # An example of how to create the ECS dyanmic target.
          # https://octopus.com/docs/infrastructure/deployment-targets/dynamic-infrastructure/new-octopustarget
          # Note that the ECS deployments are done via CloudFormation in this example project, which does not require
          # an ECS target. However, if you wish to use the "Deploy Amazon ECS Service", this target can be used.
          #read -r -d '' INPUTS <<EOF
          #{
          #    "clusterName": "app-builder-${var.github_repo_owner}-$${FIXED_ENVIRONMENT}",
          #    "authentication": {
          #      "credentials" : {
          #        "type": "account",
          #        "account": "${var.octopus_aws_account_id}"
          #      },
          #      "role": {
          #        "type": "noAssumedRole"
          #      }
          #    },
          #    "region": "$${AWS_DEFAULT_REGION}"
          #}
          #EOF

          #echo "Creating ECS target"
          #echo "##octopus[stdout-verbose]"
          #new_octopustarget --update-if-existing -n "app-builder" -t "aws-ecs-target" --inputs "$${INPUTS}" --roles "ECS Cluster"
        EOT
      }
    }
  }
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
    name                = "Deploy Load Balancer"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AwsRunCloudFormation"
      name           = "Deploy Load Balancer"
      notes          = "Deploy the load balancer via CloudFormation. This is required to expose the frontend web app and backend services under a single hostname, when then allows them to communicate via relative URL paths."
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]

      properties = {
        "Octopus.Action.Aws.AssumeRole" : "False"
        "Octopus.Action.Aws.CloudFormation.Tags" : "[]"
        "Octopus.Action.Aws.CloudFormationStackName" : "AppBuilder-ECS-LB-${lower(var.github_repo_owner)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment}"
        "Octopus.Action.Aws.CloudFormationTemplate" : <<-EOT
          Parameters:
            Vpc:
              Type: String
            SubnetA:
              Type: String
            SubnetB:
              Type: String
          Resources:
            ALBSecurityGroup:
              Type: "AWS::EC2::SecurityGroup"
              Properties:
                GroupDescription: "ALB Security group"
                GroupName: "octopub-alb-sg"
                Tags:
                  - Key: "Name"
                    Value: "octopub-alb-sg"
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
            ApplicationLoadBalancer:
              Type: "AWS::ElasticLoadBalancingV2::LoadBalancer"
              Properties:
                Name: "ECS-LB-Shared-${substr(lower(var.github_repo_owner), 0, 10)}-#{Octopus.Action[Get AWS Resources].Output.FixedEnvironment | Substring 3}"
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
          Outputs:
            Listener:
              Description: The listener
              Value: !Ref Listener
            DNSName:
              Description: The listener
              Value: !GetAtt
              - ApplicationLoadBalancer
              - DNSName
        EOT
        "Octopus.Action.Aws.CloudFormationTemplateParameters" : "[{\"ParameterKey\":\"SubnetA\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetA}\"},{\"ParameterKey\":\"SubnetB\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetB}\"},{\"ParameterKey\":\"Vpc\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.Vpc}\"}]"
        "Octopus.Action.Aws.CloudFormationTemplateParametersRaw" : "[{\"ParameterKey\":\"SubnetA\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetA}\"},{\"ParameterKey\":\"SubnetB\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.SubnetB}\"},{\"ParameterKey\":\"Vpc\",\"ParameterValue\":\"#{Octopus.Action[Get AWS Resources].Output.Vpc}\"}]"
        "Octopus.Action.Aws.IamCapabilities" : "[]"
        "Octopus.Action.Aws.Region" : var.aws_region
        "Octopus.Action.Aws.TemplateSource" : "Inline"
        "Octopus.Action.Aws.WaitForCompletion" : "True"
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False"
        "Octopus.Action.AwsAccount.Variable" : "AWS Account"
      }
    }
  }
}