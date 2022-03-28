resource "octopusdeploy_project" "deploy_infrastructure_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys the EKS cluster using eksctl. This project is created and managed by the [Octopus Terraform provider](https://registry.terraform.io/providers/OctopusDeployLabs/octopusdeploy/latest/docs). The Terraform files can be found in the [GitHub repo](https://github.com/${var.github_repo})."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_infrastructure_lifecycle_id
  name                                 = "Deploy EKS Cluster"
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
    name                = "Create an EKS cluster"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = []
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Create an EKS Cluster"
      notes          = "Create an EKS cluster with eksctl"
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
          # Get the containers
          echo "Downloading Docker images"
          echo "##octopus[stdout-verbose]"
          docker pull amazon/aws-cli 2>&1
          docker pull imega/jq 2>&1
          docker pull weaveworks/eksctl 2>&1
          echo "##octopus[stdout-default]"

          # Alias the docker run commands
          shopt -s expand_aliases
          alias aws="docker run --rm -i -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY amazon/aws-cli"
          alias eksctl="docker run --rm -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY weaveworks/eksctl"
          alias jq="docker run --rm -i imega/jq"

          # List the clusters to find out if the app-builer cluster already exists.
          # The AWS docs at https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2-docker.html say to use the "-it" docker argument.
          # This results in errors, described at https://github.com/moby/moby/issues/30137#issuecomment-736955494.
          # So we just use "-i".
          INDEX=$(aws eks list-clusters | jq '.clusters | index("app-builder-${var.github_repo_owner}")')

          # If the cluster does not exist, create it.
          if [[ $INDEX == "null" ]]; then

            # Create the eksctl config file. More information can be found at https://eksctl.io/usage/creating-and-managing-clusters/.
            cat <<EOF > cluster.yaml
          apiVersion: eksctl.io/v1alpha5
          kind: ClusterConfig

          metadata:
            name: app-builder-${var.github_repo_owner}
            region: ${var.aws_region}

          nodeGroups:
            - name: ng-1
              instanceType: t3a.small
              desiredCapacity: 2
              volumeSize: 80
              iam:
                withAddonPolicies:
                  imageBuilder: true
          EOF

            # Use eksctl to create the new cluster.
            echo "Creating the EKS cluster"
            echo "##octopus[stdout-verbose]"
            eksctl create cluster -f /build/cluster.yaml

            if [[ $? -ne 0 ]]; then
              echo "##octopus[stdout-error]"
              echo "[AppBuilder-Infrastructure-EKSFailed](https://github.com/OctopusSamples/content-team-apps/wiki/Error-Codes#appbuilder-infrastructure-eksfailed) The cluster was not created successfully. Expand the verbose logs for more details, or click the error code link for more information."
              exit 1
            fi

            echo "##octopus[stdout-default]"
          fi
        EOT
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Deploy the ALB Ingress Controller"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = []
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Deploy the ALB Ingress Controller"
      notes          = "Install the ALB ingress controller with the instructions from [here](https://docs.aws.amazon.com/eks/latest/userguide/aws-load-balancer-controller.html)"
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
          docker pull weaveworks/eksctl 2>&1
          docker pull jshimko/kube-tools-aws 2>&1
          echo "##octopus[stdout-default]"

          # Alias the docker run commands
          shopt -s expand_aliases
          alias aws="docker run --rm -i -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY amazon/aws-cli"
          alias eksctl="docker run --rm -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY weaveworks/eksctl"
          alias kubectl="docker run --rm -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY jshimko/kube-tools-aws kubectl"
          alias jq="docker run --rm -i imega/jq"

          # Extract the current AWS account
          ACCOUNT=$(aws sts get-caller-identity --query "Account" --output text)

          echo "Installing ALB Ingress Controller"
          echo "##octopus[stdout-verbose]"
          # https://docs.aws.amazon.com/eks/latest/userguide/aws-load-balancer-controller.html
          curl -o iam_policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.4.1/docs/install/iam_policy.json 2>&1

          POLICY_EXISTS=$(aws iam list-policies | jq '.Policies[] | select (.PolicyName == "AWSLoadBalancerControllerIAMPolicy")')

          if [[ -z $POLICY_EXISTS ]]; then
            aws iam create-policy \
                --policy-name AWSLoadBalancerControllerIAMPolicy \
                --policy-document file:///build/iam_policy.json
          fi

          eksctl utils associate-iam-oidc-provider \
              --region=${var.aws_region} \
              --cluster=app-builder-${var.github_repo_owner} \
              --approve

          eksctl create iamserviceaccount \
            --cluster=app-builder-${var.github_repo_owner} \
            --region=${var.aws_region} \
            --namespace=kube-system \
            --name=aws-load-balancer-controller \
            --attach-policy-arn=arn:aws:iam::$${ACCOUNT}:policy/AWSLoadBalancerControllerIAMPolicy \
            --override-existing-serviceaccounts \
            --approve

          aws eks update-kubeconfig --name app-builder-${var.github_repo_owner} --kubeconfig /build/kubeconfig

          kubectl apply \
              --kubeconfig=/build/kubeconfig \
              --validate=false \
              -f https://github.com/jetstack/cert-manager/releases/download/v1.5.4/cert-manager.yaml

          # The docs at provide instructions on downloading and modifying the ALB resources. The file in this GIST in the end result of those modifications.
          curl -Lo v2_4_1_full.yaml https://gist.githubusercontent.com/mcasperson/9edc50d87d7904d643d2f1e2f1bcc088/raw/3f24f9908ebe51fcc14aa56cc74f0b3377ea183d/v2_4_1_full.yaml 2>&1

          kubectl --kubeconfig=/build/kubeconfig apply -f /build/v2_4_1_full.yaml
          echo "##octopus[stdout-default]"

          echo "Displaying the aws-load-balancer-controller deployment"
          kubectl --kubeconfig=/build/kubeconfig get deployment -n kube-system aws-load-balancer-controller

        EOT
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Create the Backend K8s Target"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = []
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Create the Backend K8s Target"
      notes          = "Create a new Kubernetes target to deploy the backend service to."
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

          aws eks describe-cluster --name app-builder-${var.github_repo_owner} > clusterdetails.json

          echo "##octopus[create-kubernetestarget \
            name=\"$(encode_servicemessagevalue 'App Builder EKS Cluster Backend')\" \
            octopusRoles=\"$(encode_servicemessagevalue 'Kubernetes Backend,Kubernetes')\" \
            clusterName=\"$(encode_servicemessagevalue "app-builder-${var.github_repo_owner}")\" \
            clusterUrl=\"$(encode_servicemessagevalue "$(cat clusterdetails.json | jq -r '.cluster.endpoint')")\" \
            octopusAccountIdOrName=\"$(encode_servicemessagevalue "${var.octopus_aws_account_id}")\" \
            namespace=\"$(encode_servicemessagevalue '#{Octopus.Environment.Name | ToLower}-backend')\" \
            octopusDefaultWorkerPoolIdOrName=\"$(encode_servicemessagevalue "${data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id}")\" \
            updateIfExisting=\"$(encode_servicemessagevalue 'True')\" \
            skipTlsVerification=\"$(encode_servicemessagevalue 'True')\" \
            healthCheckContainerImageFeedIdOrName=\"$(encode_servicemessagevalue "${var.octopus_dockerhub_feed_id}")\" \
            healthCheckContainerImage=\"$(encode_servicemessagevalue "octopusdeploy/worker-tools:3-ubuntu.18.04")\"]"
        EOT

      }
    }
  }
  step {
    condition           = "Success"
    name                = "Create the Frontend K8s Target"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = []
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Create the Frontend K8s Target"
      notes          = "Create a new Kubernetes target to deploy the frontend webapp to."
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

          aws eks describe-cluster --name app-builder-${var.github_repo_owner} > clusterdetails.json

          echo "##octopus[create-kubernetestarget \
            name=\"$(encode_servicemessagevalue 'App Builder EKS Cluster Frontend')\" \
            octopusRoles=\"$(encode_servicemessagevalue 'Kubernetes Frontend,Kubernetes')\" \
            clusterName=\"$(encode_servicemessagevalue "app-builder-${var.github_repo_owner}")\" \
            clusterUrl=\"$(encode_servicemessagevalue "$(cat clusterdetails.json | jq -r '.cluster.endpoint')")\" \
            octopusAccountIdOrName=\"$(encode_servicemessagevalue "${var.octopus_aws_account_id}")\" \
            namespace=\"$(encode_servicemessagevalue '#{Octopus.Environment.Name | ToLower}-frontend')\" \
            octopusDefaultWorkerPoolIdOrName=\"$(encode_servicemessagevalue "${data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id}")\" \
            updateIfExisting=\"$(encode_servicemessagevalue 'True')\" \
            skipTlsVerification=\"$(encode_servicemessagevalue 'True')\" \
            healthCheckContainerImageFeedIdOrName=\"$(encode_servicemessagevalue "${var.octopus_dockerhub_feed_id}")\" \
            healthCheckContainerImage=\"$(encode_servicemessagevalue "octopusdeploy/worker-tools:3-ubuntu.18.04")\"]"
        EOT
      }
    }
  }
}