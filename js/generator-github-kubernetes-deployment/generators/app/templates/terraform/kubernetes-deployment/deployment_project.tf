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
    name                = "Deploy Backend Service"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = ["Kubernetes Backend"]
    action {
      action_type    = "Octopus.KubernetesDeployContainers"
      name           = "Deploy Backend Service"
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      features       = ["Octopus.Features.KubernetesService", "Octopus.Features.KubernetesIngress"]
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
        "Octopus.Action.KubernetesContainers.CombinedVolumes" : "[]",
        "Octopus.Action.KubernetesContainers.Containers" : "[{\"Name\":\"backend\",\"Ports\":[{\"key\":\"web\",\"keyError\":null,\"value\":\"8083\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null}],\"EnvironmentVariables\":[],\"SecretEnvironmentVariables\":[],\"ConfigMapEnvironmentVariables\":[],\"FieldRefEnvironmentVariables\":[],\"ConfigMapEnvFromSource\":[],\"SecretEnvFromSource\":[],\"VolumeMounts\":[],\"Resources\":{\"requests\":{\"memory\":\"256Mi\",\"cpu\":\"\",\"ephemeralStorage\":\"\"},\"limits\":{\"memory\":\"1Gi\",\"cpu\":\"\",\"ephemeralStorage\":\"\",\"nvidiaGpu\":\"\",\"amdGpu\":\"\"}},\"LivenessProbe\":{\"failureThreshold\":\"\",\"initialDelaySeconds\":\"\",\"periodSeconds\":\"\",\"successThreshold\":\"\",\"timeoutSeconds\":\"\",\"type\":null,\"exec\":{\"command\":[]},\"httpGet\":{\"host\":\"\",\"path\":\"\",\"port\":\"\",\"scheme\":\"\",\"httpHeaders\":[]},\"tcpSocket\":{\"host\":\"\",\"port\":\"\"}},\"ReadinessProbe\":{\"failureThreshold\":\"\",\"initialDelaySeconds\":\"\",\"periodSeconds\":\"\",\"successThreshold\":\"\",\"timeoutSeconds\":\"\",\"type\":null,\"exec\":{\"command\":[]},\"httpGet\":{\"host\":\"\",\"path\":\"\",\"port\":\"\",\"scheme\":\"\",\"httpHeaders\":[]},\"tcpSocket\":{\"host\":\"\",\"port\":\"\"}},\"StartupProbe\":{\"failureThreshold\":\"\",\"initialDelaySeconds\":\"\",\"periodSeconds\":\"\",\"successThreshold\":\"\",\"timeoutSeconds\":\"\",\"type\":null,\"exec\":{\"command\":[]},\"httpGet\":{\"host\":\"\",\"path\":\"\",\"port\":\"\",\"scheme\":\"\",\"httpHeaders\":[]},\"tcpSocket\":{\"host\":\"\",\"port\":\"\"}},\"Command\":[],\"Args\":[],\"InitContainer\":\"False\",\"SecurityContext\":{\"allowPrivilegeEscalation\":\"\",\"privileged\":\"\",\"readOnlyRootFilesystem\":\"\",\"runAsGroup\":\"\",\"runAsNonRoot\":\"\",\"runAsUser\":\"\",\"capabilities\":{\"add\":[],\"drop\":[]},\"seLinuxOptions\":{\"level\":\"\",\"role\":\"\",\"type\":\"\",\"user\":\"\"}},\"Lifecycle\":{},\"CreateFeedSecrets\":\"False\"}]",
        "Octopus.Action.KubernetesContainers.DeploymentAnnotations" : "[]",
        "Octopus.Action.KubernetesContainers.DeploymentLabels" : "{\"app\" : \"backend\"}",
        "Octopus.Action.KubernetesContainers.DeploymentName" : "backend",
        "Octopus.Action.KubernetesContainers.DeploymentResourceType" : "Deployment",
        "Octopus.Action.KubernetesContainers.DeploymentStyle" : "RollingUpdate",
        "Octopus.Action.KubernetesContainers.DeploymentWait" : "Wait",
        "Octopus.Action.KubernetesContainers.DnsConfigOptions" : "[]",
        "Octopus.Action.KubernetesContainers.IngressAnnotations": "[{\"key\":\"kubernetes.io/ingress.class\",\"keyError\":null,\"value\":\"alb\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null}]",
        "Octopus.Action.KubernetesContainers.NodeAffinity" : "[]",
        "Octopus.Action.KubernetesContainers.PersistentVolumeClaims" : "[]",
        "Octopus.Action.KubernetesContainers.PodAffinity" : "[]",
        "Octopus.Action.KubernetesContainers.PodAnnotations" : "[]",
        "Octopus.Action.KubernetesContainers.PodAntiAffinity" : "[]",
        "Octopus.Action.KubernetesContainers.PodSecuritySysctls" : "[]",
        "Octopus.Action.KubernetesContainers.Replicas" : "1",
        "Octopus.Action.KubernetesContainers.RevisionHistoryLimit" : "1",
        "Octopus.Action.KubernetesContainers.ServiceNameType" : "External",
        "Octopus.Action.KubernetesContainers.ServiceType" : "NodePort",
        "Octopus.Action.KubernetesContainers.Tolerations" : "[]",
        "OctopusUseBundledTooling" : "False",
        "Octopus.Action.KubernetesContainers.PodManagementPolicy" : "OrderedReady",
        "Octopus.Action.KubernetesContainers.IngressName" : "backend-ingress",
        "Octopus.Action.KubernetesContainers.IngressRules" : "[{\"host\":\"\",\"http\":{\"paths\":[{\"key\":\"/api/customers\",\"value\":\"web\",\"option\":\"\",\"option2\":\"ImplementationSpecific\"}]}}]",
        "Octopus.Action.KubernetesContainers.ServiceName" : "backend-service",
        "Octopus.Action.KubernetesContainers.ServicePorts" : "[{\"name\":\"web\",\"port\":\"8083\",\"targetPort\":\"\",\"nodePort\":\"\",\"protocol\":\"TCP\"}]"
      }
    }
  }
}