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

locals {
  package_name = "backend"
}

resource "octopusdeploy_deployment_process" "deploy_backend" {
  project_id = octopusdeploy_project.deploy_backend_project.id
  step {
    condition           = "Success"
    name                = "Create an EKS cluster"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    run_script_action {
      can_be_used_for_project_versioning = false
      condition                          = "Success"
      is_disabled                        = false
      is_required                        = true
      script_syntax                      = "Bash"
      name                               = "Create an EKS cluster"
      run_on_server                      = true
      script_body                        = <<-EOT
        # List the clusters to find out if the app-builer cluster already exists.
        INDEX=$(aws eks list-clusters | docker run --rm -i imega/jq '.clusters | index("app-builder-cluster")')

        # If the cluster does not exist, create it.
        if [[ $? -eq "null" ]]; then

          # Create the eksctl config file. More information can be found at https://eksctl.io/usage/creating-and-managing-clusters/.
          cat <<EOF > cluster.yaml
          apiVersion: eksctl.io/v1alpha5
          kind: ClusterConfig

          metadata:
            name: app-builder-cluster
            region: ${var.aws_region}

          nodeGroups:
            - name: ng-1
              instanceType: t3a.small
              desiredCapacity: 2
              volumeSize: 80
          EOF

          # Use eksctl to create the new cluster.
          docker run --rm -e AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} -e AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY} -v $(pwd):/var/opt/eksctl weaveworks/eksctl create cluster -f /var/opt/eksctl/cluster.yaml

      fi

      aws eks describe-cluster --name app-builder-cluster > clusterdetails.json

      echo "##octopus[create-kubernetestarget \
        name=\"$(encode_servicemessagevalue 'App Builder EKS CLuster')\" \
        octopusRoles=\"$(encode_servicemessagevalue 'Kubernetes')\" \
        clusterName=\"$(encode_servicemessagevalue "app-builder-cluster")\" \
        clusterUrl=\"$(encode_servicemessagevalue "$(cat clusterdetails.json | docker run --rm -i imega/jq -r '.cluster.endpoint')\" \
        octopusAccountIdOrName=\"$(encode_servicemessagevalue "${var.octopus_aws_account_id}")\" \
        namespace=\"$(encode_servicemessagevalue '#{Octopus.Environment.Name | ToLower}-backend')\" \
        octopusDefaultWorkerPoolIdOrName=\"$(encode_servicemessagevalue "${data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id}")\" \
        updateIfExisting=\"$(encode_servicemessagevalue 'True')\" \
        skipTlsVerification=\"$(encode_servicemessagevalue 'True')\"]"

      EOT
    }
  }
  step {
    condition           = "Success"
    name                = "Deploy Backend Service"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartWithPrevious"
    target_roles        = ["Kubernetes"]
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