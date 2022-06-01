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
      SUBNETA=$(aws ec2 describe-subnets --filter "Name=tag:aws:cloudformation:stack-name,Values=amazon-ecs-cli-setup-app-builder-${var.github_repo_owner}-$${FIXED_ENVIRONMENT}" | jq -r '.Subnets[0].SubnetId')
      SUBNETB=$(aws ec2 describe-subnets --filter "Name=tag:aws:cloudformation:stack-name,Values=amazon-ecs-cli-setup-app-builder-${var.github_repo_owner}-$${FIXED_ENVIRONMENT}" | jq -r '.Subnets[1].SubnetId')
      VPC=$(aws ec2 describe-vpcs --filter "Name=tag:aws:cloudformation:stack-name,Values=amazon-ecs-cli-setup-app-builder-${var.github_repo_owner}-$${FIXED_ENVIRONMENT}" | jq -r '.Vpcs[0].VpcId')
      SECURITYGROUP=$(aws ec2 describe-security-groups --filters Name=vpc-id,Values=$${VPC} Name=group-name,Values=default | jq -r '.SecurityGroups[].GroupId')

      # The load balancer listener was created by the infrastructure deployment project, and is read from the CloudFormation stack outputs.
      # Note these values will be empty for the first run of the ecs infrastructure script.
      LISTENER=$(aws cloudformation describe-stacks --stack-name "AppBuilder-ECS-LB-${lower(var.github_repo_owner)}-$${FIXED_ENVIRONMENT}" --query "Stacks[0].Outputs[?OutputKey=='Listener'].OutputValue" --output text 2>/dev/null)
      DNSNAME=$(aws cloudformation describe-stacks --stack-name "AppBuilder-ECS-LB-${lower(var.github_repo_owner)}-$${FIXED_ENVIRONMENT}" --query "Stacks[0].Outputs[?OutputKey=='DNSName'].OutputValue" --output text 2>/dev/null)

      echo "##octopus[stdout-default]"

      echo "Found Security Group: $${SECURITYGROUP}"
      echo "Found Subnet A: $${SUBNETA}"
      echo "Found Subnet B: $${SUBNETB}"
      echo "Found VPC: $${VPC}"
      echo "Found Listener: $${LISTENER}"
      echo "Found Main Load Balancer DNS Name: $${DNSNAME}"

      set_octopusvariable "SecurityGroup" "$${SECURITYGROUP}"
      set_octopusvariable "SubnetA" "$${SUBNETA}"
      set_octopusvariable "SubnetB" "$${SUBNETB}"
      set_octopusvariable "Vpc" "$${VPC}"
      set_octopusvariable "ClusterName" "app-builder-${var.github_repo_owner}-$${FIXED_ENVIRONMENT}"
      set_octopusvariable "FixedEnvironment" "$${FIXED_ENVIRONMENT}"
      set_octopusvariable "Listener" $${LISTENER}
      set_octopusvariable "DNSName" "$${DNSNAME}"

      if [[ -z $${SECURITYGROUP} || -z $${SUBNETA} || -z $${SUBNETB} ]]; then
        write_highlight "[AppBuilder-Infrastructure-ECSResourceLookupFailed](https://github.com/OctopusSamples/content-team-apps/wiki/Error-Codes#appbuilder-infrastructure-ecsresourcelookupfailed) Failed to find one of the resources created with the ECS cluster."
        exit 1
      fi
    EOT

  vulnerability_scan = <<-EOT
      echo "##octopus[stdout-verbose]"
      docker pull appthreat/dep-scan
      echo "##octopus[stdout-default]"

      TIMESTAMP=$(date +%s%3N)
      SUCCESS=0
      for x in $(find . -name bom.xml -type f -print); do
          echo "Scanning $${x}"

          # Delete any existing report file
          if [[ -f "$PWD/depscan-bom.json" ]]; then
            rm "$PWD/depscan-bom.json"
          fi

          # Generate the report, capturing the output, and ensuring $? is set to the exit code
          OUTPUT=$(bash -c "docker run --rm -v \"$PWD:/app\" appthreat/dep-scan scan --bom \"/app/$${x}\" --type bom --report_file /app/depscan.json; exit \$?" 2>&1)

          # Success is set to 1 if the exit code is not zero
          if [[ $? -ne 0 ]]; then
              SUCCESS=1
          fi

          # Print the output stripped of ANSI colour codes
          echo -e "$${OUTPUT}" | sed 's/\x1b\[[0-9;]*m//g'
      done

      set_octopusvariable "VerificationResult" $SUCCESS

      if [[ $SUCCESS -ne 0 ]]; then
        >&2 echo "Critical vulnerabilities were detected"
      else
        echo "No critical vulnerabilities were detected"
      fi

      exit 0
    EOT
}