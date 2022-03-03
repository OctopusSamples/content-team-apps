resource "octopusdeploy_project" "deploy_backend_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys the backend service."
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

  connectivity_policy {
    allow_deployments_to_no_targets = false
    exclude_unhealthy_targets       = false
    skip_machine_behavior           = "SkipUnavailableMachines"
  }
}

output "deploy_backend_project_id" {
  value = octopusdeploy_project.deploy_backend_project.id
}

resource "octopusdeploy_variable" "aws_account_deploy_backend_project" {
  name     = "AWS Account"
  type     = "AmazonWebServicesAccount"
  value    = var.octopus_aws_account_id
  owner_id = octopusdeploy_project.deploy_infrastructure_project.id
}

locals {
  package_name = "backend"
}

resource "octopusdeploy_deployment_process" "deploy_backend" {
  project_id = octopusdeploy_project.deploy_backend_project.id
  step {
    condition           = "Success"
    name                = "Create the K8s Target"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = []
    action {
      action_type    = "Octopus.AwsRunScript"
      name           = "Create the K8s Target"
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
        "Octopus.Action.Script.ScriptBody": "# Get the containers\necho \"Downloading Docker images\"\necho \"##octopus[stdout-verbose]\"\ndocker pull amazon/aws-cli 2>&1 \ndocker pull imega/jq 2>&1 \necho \"##octopus[stdout-default]\"\n\n# Alias the docker run commands\nshopt -s expand_aliases\nalias aws=\"docker run --rm -i -v $(pwd):/build -e AWS_DEFAULT_REGION=$AWS_DEFAULT_REGION -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY amazon/aws-cli\"\nalias jq=\"docker run --rm -i imega/jq\"\n\naws eks describe-cluster --name app-builder-cluster > clusterdetails.json\n\necho \"##octopus[create-kubernetestarget \\\n  name=\\\"$(encode_servicemessagevalue 'App Builder EKS Cluster')\\\" \\\n  octopusRoles=\\\"$(encode_servicemessagevalue 'Kubernetes Backend,Kubernetes')\\\" \\\n  clusterName=\\\"$(encode_servicemessagevalue \"app-builder-cluster\")\\\" \\\n  clusterUrl=\\\"$(encode_servicemessagevalue \"$(cat clusterdetails.json | jq -r '.cluster.endpoint')\")\\\" \\\n  octopusAccountIdOrName=\\\"$(encode_servicemessagevalue \"${var.octopus_aws_account_id}\")\\\" \\\n  namespace=\\\"$(encode_servicemessagevalue '#{Octopus.Environment.Name | ToLower}-backend')\\\" \\\n  octopusDefaultWorkerPoolIdOrName=\\\"$(encode_servicemessagevalue \"${data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id}\")\\\" \\\n  updateIfExisting=\\\"$(encode_servicemessagevalue 'True')\\\" \\\n  skipTlsVerification=\\\"$(encode_servicemessagevalue 'True')\\\" \\\n  healthCheckContainerImageFeedIdOrName=\\\"$(encode_servicemessagevalue \"${var.octopus_dockerhub_feed_id}\")\\\" \\\n  healthCheckContainerImage=\\\"$(encode_servicemessagevalue \"octopusdeploy/worker-tools:3-ubuntu.18.04\")\\\"]\"",
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Deploy Backend Service"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = ["Kubernetes Backend"]
    action {
      action_type    = "Octopus.KubernetesDeployContainers"
      name           = "Deploy Backend Service"
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      package {
        name                      = local.package_name
        package_id                = var.octopus_docker_image
        feed_id                   = var.octopus_k8s_feed_id
        acquisition_location      = "NotAcquired"
        extract_during_deployment = false
      }
      container {
        feed_id = var.octopus_dockerhub_feed_id
        image   = "octopusdeploy/worker-tools:3-ubuntu.18.04"
      }
      properties = {
        "Octopus.Action.EnabledFeatures" : ",Octopus.Features.KubernetesService,Octopus.Features.KubernetesIngress,Octopus.Features.KubernetesConfigMap,Octopus.Features.KubernetesSecret"
        "Octopus.Action.KubernetesContainers.Replicas" : "1"
        "Octopus.Action.KubernetesContainers.DeploymentStyle" : "RollingUpdate"
        "Octopus.Action.KubernetesContainers.ServiceNameType" : "External"
        "Octopus.Action.KubernetesContainers.DeploymentResourceType" : "Deployment"
        "Octopus.Action.KubernetesContainers.DeploymentWait" : "Wait"
        "Octopus.Action.KubernetesContainers.ServiceType" : "ClusterIP"
        "Octopus.Action.KubernetesContainers.IngressAnnotations" : "[]"
        "Octopus.Action.KubernetesContainers.PersistentVolumeClaims" : "[]"
        "Octopus.Action.KubernetesContainers.Tolerations" : "[]"
        "Octopus.Action.KubernetesContainers.NodeAffinity" : "[]"
        "Octopus.Action.KubernetesContainers.PodAffinity" : "[]"
        "Octopus.Action.KubernetesContainers.PodAntiAffinity" : "[]"
        "Octopus.Action.KubernetesContainers.DeploymentName" : "backend"
        "Octopus.Action.KubernetesContainers.RevisionHistoryLimit" : "1"
        "Octopus.Action.KubernetesContainers.DnsConfigOptions" : "[]"
        "Octopus.Action.KubernetesContainers.PodAnnotations" : "[]"
        "Octopus.Action.KubernetesContainers.DeploymentAnnotations" : "[]"
        "Octopus.Action.KubernetesContainers.DeploymentLabels" : "{\"app\" : \"backend\"}"
        "Octopus.Action.KubernetesContainers.CombinedVolumes" : "[]"
        "Octopus.Action.KubernetesContainers.PodSecuritySysctls" : "[]"
        "Octopus.Action.KubernetesContainers.Containers": "[{\"Name\":\"${local.package_name}\",\"Ports\":[{\"key\":\"web\",\"keyError\":null,\"value\":\"8083\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null}],\"EnvironmentVariables\":[],\"SecretEnvironmentVariables\":[],\"ConfigMapEnvironmentVariables\":[],\"FieldRefEnvironmentVariables\":[],\"ConfigMapEnvFromSource\":[],\"SecretEnvFromSource\":[],\"VolumeMounts\":[],\"Resources\":{\"requests\":{\"memory\":\"256Mi\",\"cpu\":\"\",\"ephemeralStorage\":\"\"},\"limits\":{\"memory\":\"1Gi\",\"cpu\":\"\",\"ephemeralStorage\":\"\",\"nvidiaGpu\":\"\",\"amdGpu\":\"\"}},\"LivenessProbe\":{\"failureThreshold\":\"\",\"initialDelaySeconds\":\"\",\"periodSeconds\":\"\",\"successThreshold\":\"\",\"timeoutSeconds\":\"\",\"type\":null,\"exec\":{\"command\":[]},\"httpGet\":{\"host\":\"\",\"path\":\"\",\"port\":\"\",\"scheme\":\"\",\"httpHeaders\":[]},\"tcpSocket\":{\"host\":\"\",\"port\":\"\"}},\"ReadinessProbe\":{\"failureThreshold\":\"\",\"initialDelaySeconds\":\"\",\"periodSeconds\":\"\",\"successThreshold\":\"\",\"timeoutSeconds\":\"\",\"type\":null,\"exec\":{\"command\":[]},\"httpGet\":{\"host\":\"\",\"path\":\"\",\"port\":\"\",\"scheme\":\"\",\"httpHeaders\":[]},\"tcpSocket\":{\"host\":\"\",\"port\":\"\"}},\"StartupProbe\":{\"failureThreshold\":\"\",\"initialDelaySeconds\":\"\",\"periodSeconds\":\"\",\"successThreshold\":\"\",\"timeoutSeconds\":\"\",\"type\":null,\"exec\":{\"command\":[]},\"httpGet\":{\"host\":\"\",\"path\":\"\",\"port\":\"\",\"scheme\":\"\",\"httpHeaders\":[]},\"tcpSocket\":{\"host\":\"\",\"port\":\"\"}},\"Command\":[],\"Args\":[],\"InitContainer\":\"False\",\"SecurityContext\":{\"allowPrivilegeEscalation\":\"\",\"privileged\":\"\",\"readOnlyRootFilesystem\":\"\",\"runAsGroup\":\"\",\"runAsNonRoot\":\"\",\"runAsUser\":\"\",\"capabilities\":{\"add\":[],\"drop\":[]},\"seLinuxOptions\":{\"level\":\"\",\"role\":\"\",\"type\":\"\",\"user\":\"\"}},\"Lifecycle\":{},\"CreateFeedSecrets\":\"False\"}]"
      }
    }
  }
}