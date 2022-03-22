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
        "Octopus.Action.Aws.Region" : "${var.aws_region}",
        "Octopus.Action.Script.ScriptBody": "# Get the containers\necho \"Downloading Docker images\"\necho \"##octopus[stdout-verbose]\"\ndocker pull amazon/aws-cli 2>&1 \ndocker pull imega/jq 2>&1 \ndocker pull weaveworks/eksctl 2>&1\necho \"##octopus[stdout-default]\"\n\n# Alias the docker run commands\nshopt -s expand_aliases\nalias aws=\"docker run --rm -i -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY amazon/aws-cli\"\nalias eksctl=\"docker run --rm -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY weaveworks/eksctl\"\nalias jq=\"docker run --rm -i imega/jq\"\n\n# List the clusters to find out if the app-builer cluster already exists.\n# The AWS docs at https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2-docker.html say to use the \"-it\" docker argument.\n# This results in errors, described at https://github.com/moby/moby/issues/30137#issuecomment-736955494.\n# So we just use \"-i\".\nINDEX=$(aws eks list-clusters | jq '.clusters | index(\"app-builder-cluster\")')\n\n# If the cluster does not exist, create it.\nif [[ $INDEX -eq \"null\" ]]; then\n\n  # Create the eksctl config file. More information can be found at https://eksctl.io/usage/creating-and-managing-clusters/.\n  cat <<EOF > cluster.yaml\napiVersion: eksctl.io/v1alpha5\nkind: ClusterConfig\n\nmetadata:\n  name: app-builder-cluster\n  region: ${var.aws_region}\n\nnodeGroups:\n  - name: ng-1\n    instanceType: t3a.small\n    desiredCapacity: 2\n    volumeSize: 80\n    iam:\n      withAddonPolicies:\n        imageBuilder: true\nEOF\n\n  # Use eksctl to create the new cluster.\n  echo \"Create the EKS cluster\"\n  echo \"##octopus[stdout-verbose]\"\n  eksctl create cluster -f /build/cluster.yaml\n  echo \"##octopus[stdout-default]\"\n\nfi",
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
      notes          = "Install the ALB ingress controller with the instructions from https://docs.aws.amazon.com/eks/latest/userguide/aws-load-balancer-controller.html"
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      properties     = {
        "OctopusUseBundledTooling" : "False",
        "Octopus.Action.Script.ScriptSource" : "Inline",
        "Octopus.Action.Script.Syntax" : "Bash",
        "Octopus.Action.Aws.AssumeRole" : "False",
        "Octopus.Action.AwsAccount.UseInstanceRole" : "False",
        "Octopus.Action.AwsAccount.Variable" : "AWS Account",
        "Octopus.Action.Aws.Region" : "${var.aws_region}",
        "Octopus.Action.Script.ScriptBody" : "# Get the containers\necho \"Downloading Docker images\"\necho \"##octopus[stdout-verbose]\"\ndocker pull amazon/aws-cli 2>&1 \ndocker pull imega/jq 2>&1 \ndocker pull weaveworks/eksctl 2>&1 \ndocker pull jshimko/kube-tools-aws 2>&1\necho \"##octopus[stdout-default]\"\n\n# Alias the docker run commands\nshopt -s expand_aliases\nalias aws=\"docker run --rm -i -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY amazon/aws-cli\"\nalias eksctl=\"docker run --rm -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY weaveworks/eksctl\"\nalias kubectl=\"docker run --rm -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY jshimko/kube-tools-aws kubectl\"\nalias jq=\"docker run --rm -i imega/jq\"\n\n# Extract the current AWS account\nACCOUNT=$(aws sts get-caller-identity --query \"Account\" --output text)\n\necho \"Installing ALB Ingress Controller\"\necho \"##octopus[stdout-verbose]\"\n# https://docs.aws.amazon.com/eks/latest/userguide/aws-load-balancer-controller.html\ncurl -o iam_policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/v2.4.0/docs/install/iam_policy.json 2>&1\n\nPOLICY_EXISTS=$(aws iam list-policies | jq '.Policies[] | select (.PolicyName == \"AWSLoadBalancerControllerIAMPolicy\")')\n\nif [[ -z $POLICY_EXISTS ]]; then\n  aws iam create-policy \\\n      --policy-name AWSLoadBalancerControllerIAMPolicy \\\n      --policy-document file:///build/iam_policy.json\nfi      \n    \neksctl utils associate-iam-oidc-provider \\\n\t--region=${var.aws_region} \\\n    --cluster=app-builder-cluster \\\n    --approve\n    \neksctl create iamserviceaccount \\\n  --cluster=app-builder-cluster \\\n  --region=${var.aws_region} \\\n  --namespace=kube-system \\\n  --name=aws-load-balancer-controller \\\n  --attach-policy-arn=arn:aws:iam::$${ACCOUNT}:policy/AWSLoadBalancerControllerIAMPolicy \\\n  --override-existing-serviceaccounts \\\n  --approve\n  \naws\teks update-kubeconfig --name app-builder-cluster --kubeconfig /build/kubeconfig\n\nkubectl apply \\\n\t--kubeconfig=/build/kubeconfig \\\n    --validate=false \\\n    -f https://github.com/jetstack/cert-manager/releases/download/v1.5.4/cert-manager.yaml\n\n# The docs at provide instructions on downloading and modifying the ALB resources. The file in this GIST in the end result of those modifications.\ncurl -Lo v2_4_0_full.yaml https://gist.githubusercontent.com/mcasperson/e865e5567b1fbff2e969cdf33f0908f7/raw/f21059ce64c84c856ec85e3d63523ce3de45e36a/v2_4_0_full.yaml 2>&1\n\nkubectl --kubeconfig=/build/kubeconfig apply -f /build/v2_4_0_full.yaml\necho \"##octopus[stdout-default]\"\n\necho \"Displaying the aws-load-balancer-controller deployment\"\nkubectl --kubeconfig=/build/kubeconfig get deployment -n kube-system aws-load-balancer-controller\n",
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
        "Octopus.Action.Aws.Region" : "${var.aws_region}",
        "Octopus.Action.Script.ScriptBody": "# Get the containers\necho \"Downloading Docker images\"\necho \"##octopus[stdout-verbose]\"\ndocker pull amazon/aws-cli 2>&1 \ndocker pull imega/jq 2>&1 \necho \"##octopus[stdout-default]\"\n\n# Alias the docker run commands\nshopt -s expand_aliases\nalias aws=\"docker run --rm -i -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY amazon/aws-cli\"\nalias jq=\"docker run --rm -i imega/jq\"\n\naws eks describe-cluster --name app-builder-cluster > clusterdetails.json\n\necho \"##octopus[create-kubernetestarget \\\n  name=\\\"$(encode_servicemessagevalue 'App Builder EKS Cluster Backend')\\\" \\\n  octopusRoles=\\\"$(encode_servicemessagevalue 'Kubernetes Backend,Kubernetes')\\\" \\\n  clusterName=\\\"$(encode_servicemessagevalue \"app-builder-cluster\")\\\" \\\n  clusterUrl=\\\"$(encode_servicemessagevalue \"$(cat clusterdetails.json | jq -r '.cluster.endpoint')\")\\\" \\\n  octopusAccountIdOrName=\\\"$(encode_servicemessagevalue \"${var.octopus_aws_account_id}\")\\\" \\\n  namespace=\\\"$(encode_servicemessagevalue '#{Octopus.Environment.Name | ToLower}-backend')\\\" \\\n  octopusDefaultWorkerPoolIdOrName=\\\"$(encode_servicemessagevalue \"${data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id}\")\\\" \\\n  updateIfExisting=\\\"$(encode_servicemessagevalue 'True')\\\" \\\n  skipTlsVerification=\\\"$(encode_servicemessagevalue 'True')\\\" \\\n  healthCheckContainerImageFeedIdOrName=\\\"$(encode_servicemessagevalue \"${var.octopus_dockerhub_feed_id}\")\\\" \\\n  healthCheckContainerImage=\\\"$(encode_servicemessagevalue \"octopusdeploy/worker-tools:3-ubuntu.18.04\")\\\"]\"",
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
        "Octopus.Action.Aws.Region" : "${var.aws_region}",
        "Octopus.Action.Script.ScriptBody": "# Get the containers\necho \"Downloading Docker images\"\necho \"##octopus[stdout-verbose]\"\ndocker pull amazon/aws-cli 2>&1 \ndocker pull imega/jq 2>&1 \necho \"##octopus[stdout-default]\"\n\n# Alias the docker run commands\nshopt -s expand_aliases\nalias aws=\"docker run --rm -i -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY amazon/aws-cli\"\nalias jq=\"docker run --rm -i imega/jq\"\n\naws eks describe-cluster --name app-builder-cluster > clusterdetails.json\n\necho \"##octopus[create-kubernetestarget \\\n  name=\\\"$(encode_servicemessagevalue 'App Builder EKS Cluster Frontend')\\\" \\\n  octopusRoles=\\\"$(encode_servicemessagevalue 'Kubernetes Frontend,Kubernetes')\\\" \\\n  clusterName=\\\"$(encode_servicemessagevalue \"app-builder-cluster\")\\\" \\\n  clusterUrl=\\\"$(encode_servicemessagevalue \"$(cat clusterdetails.json | jq -r '.cluster.endpoint')\")\\\" \\\n  octopusAccountIdOrName=\\\"$(encode_servicemessagevalue \"${var.octopus_aws_account_id}\")\\\" \\\n  namespace=\\\"$(encode_servicemessagevalue '#{Octopus.Environment.Name | ToLower}-frontend')\\\" \\\n  octopusDefaultWorkerPoolIdOrName=\\\"$(encode_servicemessagevalue \"${data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id}\")\\\" \\\n  updateIfExisting=\\\"$(encode_servicemessagevalue 'True')\\\" \\\n  skipTlsVerification=\\\"$(encode_servicemessagevalue 'True')\\\" \\\n  healthCheckContainerImageFeedIdOrName=\\\"$(encode_servicemessagevalue \"${var.octopus_dockerhub_feed_id}\")\\\" \\\n  healthCheckContainerImage=\\\"$(encode_servicemessagevalue \"octopusdeploy/worker-tools:3-ubuntu.18.04\")\\\"]\"",
      }
    }
  }
}