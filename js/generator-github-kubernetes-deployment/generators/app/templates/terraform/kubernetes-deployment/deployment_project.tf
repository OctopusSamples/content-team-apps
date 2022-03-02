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

  connectivity_policy {
    allow_deployments_to_no_targets = false
    exclude_unhealthy_targets       = false
    skip_machine_behavior           = "SkipUnavailableMachines"
  }
}

output "deploy_backend_project_id" {
  value = octopusdeploy_project.deploy_backend_project.id
}

locals {
  package_name = "backend"
}

resource "octopusdeploy_deployment_process" "deploy_backend_step1" {
  project_id = octopusdeploy_project.deploy_backend_project.id
  step {
    condition           = "Success"
    name                = "Deploy Backend Service"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartWithPrevious"
    target_roles        = ["App Builder EKS"]
    action {
      action_type    = "Octopus.KubernetesDeployContainers"
      name           = "Deploy Backend Service"
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      package {
        name                      = local.package_name
        package_id                = var.octopus_docker_image
        feed_id                   = var.octopus_github_docker_feed_id
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
        "Octopus.Action.KubernetesContainers.Containers" : "[{\"IsNew\" : true,\"InitContainer\" : \"False\",\"Ports\" : [],\"EnvironmentVariables\" : [],\"SecretEnvironmentVariables\" : [],\"SecretEnvFromSource\" : [],\"ConfigMapEnvironmentVariables\" : [],\"ConfigMapEnvFromSource\" : [],\"FieldRefEnvironmentVariables\" : [],\"VolumeMounts\" : [],\"AcquisitionLocation\" : \"NotAcquired\",\"Name\" : \"${local.package_name}\",\"PackageId\" : \"${var.octopus_docker_image}\",\"FeedId\" : \"${var.octopus_github_docker_feed_id}\",\"Properties\" : {},\"Command\" : [],\"Args\" : [],\"Resources\" : {\"requests\" : {\"memory\" : \"256Mi\",\"cpu\" : \"\",\"ephemeralStorage\" : \"\"},\"limits\" : {\"memory\" : \"1Gi\",\"cpu\" : \"\",\"ephemeralStorage\" : \"\",\"nvidiaGpu\" : \"\",\"amdGpu\" : \"\"}},\"LivenessProbe\" : {\"failureThreshold\" : \"\",\"initialDelaySeconds\" : \"\",\"periodSeconds\" : \"\",\"successThreshold\" : \"\",\"timeoutSeconds\" : \"\",\"type\" : null,\"exec\" : {\"command\" : []},\"httpGet\" : {\"host\" : \"\",\"path\" : \"\",\"port\" : \"\",\"scheme\" : \"\",\"httpHeaders\" : []},\"tcpSocket\" : {\"host\" : \"\",\"port\" : \"\"}},\"ReadinessProbe\" : {\"failureThreshold\" : \"\",\"initialDelaySeconds\" : \"\",\"periodSeconds\" : \"\",\"successThreshold\" : \"\",\"timeoutSeconds\" : \"\",\"type\" : null,\"exec\" : {\"command\" : []},\"httpGet\" : {\"host\" : \"\",\"path\" : \"\",\"port\" : \"\",\"scheme\" : \"\",\"httpHeaders\" : []},\"tcpSocket\" : {\"host\" : \"\",\"port\" : \"\"}},\"StartupProbe\" : {\"failureThreshold\" : \"\",\"initialDelaySeconds\" : \"\",\"periodSeconds\" : \"\",\"successThreshold\" : \"\",\"timeoutSeconds\" : \"\",\"type\" : null,\"exec\" : {\"command\" : []},\"httpGet\" : {\"host\" : \"\",\"path\" : \"\",\"port\" : \"\",\"scheme\" : \"\",\"httpHeaders\" : []},\"tcpSocket\" : {\"host\" : \"\",\"port\" : \"\"}},\"Lifecycle\" : {},\"SecurityContext\" : {\"allowPrivilegeEscalation\" : \"\",\"privileged\" : \"\",\"readOnlyRootFilesystem\" : \"\",\"runAsGroup\" : \"\",\"runAsNonRoot\" : \"\",\"runAsUser\" : \"\",\"capabilities\" : {\"add\" : [],\"drop\" : []},\"seLinuxOptions\" : {\"level\" : \"\",\"role\" : \"\",\"type\" : \"\",\"user\" : \"\"}}}]"
      }
    }
  }
}