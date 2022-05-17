locals {
  get_aws_resources = <<-EOT
      # Get the containers
      echo "Downloading Docker images"

      echo "##octopus[stdout-verbose]"

      docker pull amazon/aws-cli 2>&1
      docker pull imega/jq 2>&1

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
      LISTENER=$(aws cloudformation describe-stacks --stack-name "AppBuilder-ECS-LB-${lower(var.github_repo_owner)}-$${FIXED_ENVIRONMENT}" --query "Stacks[0].Outputs[?OutputKey=='Listener'].OutputValue" --output text)

      echo "##octopus[stdout-default]"

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