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
  name                                 = "Create ECS Cluster"
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

          # Create the cluster using the instructions from https://docs.aws.amazon.com/AmazonECS/latest/developerguide/ecs-cli-tutorial-fargate.html
          EXISTING=$(aws iam list-roles --max-items 10000 | jq -r '.Roles[] | select(.RoleName == "ecsTaskExecutionRole") | .Arn')
          if [[ -z "$${EXISTING}" ]]; then
            echo "Creating IAM role"
            echo "##octopus[stdout-verbose]"

            cat <<EOF > ecsTaskExecutionRole.json
            {
              "Version": "2012-10-17",
              "Statement": [
                {
                  "Sid": "",
                  "Effect": "Allow",
                  "Principal": {
                    "Service": "ecs-tasks.amazonaws.com"
                  },
                  "Action": "sts:AssumeRole"
                }
              ]
            }
          EOF

            aws iam create-role --role-name ecsTaskExecutionRole --assume-role-policy-document file://build/ecsTaskExecutionRole.json
            aws iam attach-role-policy --role-name ecsTaskExecutionRole --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy
            echo "##octopus[stdout-default]"
          else
            echo "IAM role already exists with ARN $${EXISTING}"
          fi

          # Find any existing cluster with the name "app-builder".
          EXISTINGCLUSTER=$(aws ecs list-clusters | jq -r '.clusterArns[] | select(. | endswith("/app-builder-${var.github_repo_owner}"))')

          # If the cluster does not exist, create it.
          if [[ -z "$${EXISTINGCLUSTER}" ]]; then
            echo "Creating ECS cluster"
            echo "##octopus[stdout-verbose]"

            ./ecs-cli configure --cluster app-builder --default-launch-type FARGATE --config-name app-builder-${var.github_repo_owner} --region $${AWS_DEFAULT_REGION}
            ./ecs-cli configure profile --access-key $${AWS_ACCESS_KEY_ID} --secret-key $${AWS_SECRET_ACCESS_KEY} --profile-name app-builder-${var.github_repo_owner}-profile
            ./ecs-cli up --cluster-config app-builder-${var.github_repo_owner} --ecs-profile app-builder-${var.github_repo_owner}-profile --tags 'CreatedBy=AppBuilder,TargetType=ECS' > output.txt

            if [[ $? -ne 0 ]]; then
              echo "[AppBuilder-Infrastructure-ECSFailed](https://github.com/OctopusSamples/content-team-apps/wiki/Error-Codes#appbuilder-infrastructure-ecsfailed) Failed to create the cluster with ecs-cli."
              exit 1
            fi

            # Extract the VPC and subnets from the ecs-cli output text.
            VPC=$(awk '/VPC created:/{print $NF}' output.txt)
            SUBNETS=$(awk '/Subnet created:/{print $NF}' output.txt)

            # Find the security group associated with the VPC.
            SECURITYGROUP=$(aws ec2 describe-security-groups --filters Name=vpc-id,Values=$${VPC} | jq -r '.SecurityGroups[].GroupId')

            # Expose port 8083, which is used by the sample backend service.
            aws ec2 authorize-security-group-ingress --group-id $${SECURITYGROUP} --protocol tcp --port 8083 --cidr 0.0.0.0/0
            echo "##octopus[stdout-default]"
          else
            echo "ECS Cluster already exists with ARN $${EXISTINGCLUSTER}"
          fi

          # Create the dyanmic target.
          # https://octopus.com/docs/infrastructure/deployment-targets/dynamic-infrastructure/new-octopustarget
          # Note that the ECS deployments are done via CloudFormation in this example project, which does not require
          # an ECS target. However, if you wish to use the "Deploy Amazon ECS Service", this target can be used.
          read -r -d '' INPUTS <<EOF
          {
              "clusterName": "app-builder-${var.github_repo_owner}",
              "awsAccount": "${var.octopus_aws_account_id}",
              "region": "$${AWS_DEFAULT_REGION}"
          }
          EOF

          echo "Creating ECS target"
          echo "##octopus[stdout-verbose]"
          new_octopustarget --update-if-existing -n "app-builder" -t "aws-ecs-target" --inputs "$${INPUTS}" --roles "ECS Cluster"
        EOT
      }
    }
  }
}