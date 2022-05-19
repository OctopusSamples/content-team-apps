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
  versioning_strategy {
    template = "#{Octopus.Version.LastMajor}.#{Octopus.Version.LastMinor}.#{Octopus.Version.LastPatch}.#{Octopus.Version.NextRevision}"
  }

  connectivity_policy {
    allow_deployments_to_no_targets = false
    exclude_unhealthy_targets       = false
    skip_machine_behavior           = "SkipUnavailableMachines"
  }
}

resource "octopusdeploy_variable" "debug_variable" {
  name         = "OctopusPrintVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_backend_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "debug_evaluated_variable" {
  name         = "OctopusPrintEvaluatedVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_backend_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "postman_raw_url_variable" {
  name         = "item:0:request:url:raw"
  type         = "String"
  description  = "A structured variable replacement for the Postman test."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_backend_project.id
  value        = "http://#{Octopus.Action[Display the Ingress URL].Output.DNSName}/api/products/"
}

resource "octopusdeploy_variable" "postman_raw_host_variable" {
  name         = "item:0:request:url:host:0"
  type         = "String"
  description  = "A structured variable replacement for the Postman test."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_backend_project.id
  value        = "#{Octopus.Action[Display the Ingress URL].Output.DNSName}"
}

resource "octopusdeploy_variable" "postman_raw_port_variable" {
  name         = "item:0:request:url:port"
  type         = "String"
  description  = "A structured variable replacement for the Postman test."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_backend_project.id
  value        = "80"
}

locals {
  backend_package_name = "backend"
  backend_service_name = "backend-service"
  backend_ingress_name = "backend-ingress"
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
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]
      features = ["Octopus.Features.KubernetesService", "Octopus.Features.KubernetesIngress"]
      package {
        name                      = local.backend_package_name
        package_id                = var.backend_docker_image
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
        "Octopus.Action.KubernetesContainers.Containers" : "[{\"Name\":\"${local.backend_package_name}\",\"Ports\":[{\"key\":\"web\",\"keyError\":null,\"value\":\"8083\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null}],\"EnvironmentVariables\":[],\"SecretEnvironmentVariables\":[],\"ConfigMapEnvironmentVariables\":[],\"FieldRefEnvironmentVariables\":[],\"ConfigMapEnvFromSource\":[],\"SecretEnvFromSource\":[],\"VolumeMounts\":[],\"Resources\":{\"requests\":{\"memory\":\"256Mi\",\"cpu\":\"\",\"ephemeralStorage\":\"\"},\"limits\":{\"memory\":\"1Gi\",\"cpu\":\"\",\"ephemeralStorage\":\"\",\"nvidiaGpu\":\"\",\"amdGpu\":\"\"}},\"LivenessProbe\":{\"failureThreshold\":\"\",\"initialDelaySeconds\":\"\",\"periodSeconds\":\"\",\"successThreshold\":\"\",\"timeoutSeconds\":\"\",\"type\":null,\"exec\":{\"command\":[]},\"httpGet\":{\"host\":\"\",\"path\":\"\",\"port\":\"\",\"scheme\":\"\",\"httpHeaders\":[]},\"tcpSocket\":{\"host\":\"\",\"port\":\"\"}},\"ReadinessProbe\":{\"failureThreshold\":\"\",\"initialDelaySeconds\":\"\",\"periodSeconds\":\"\",\"successThreshold\":\"\",\"timeoutSeconds\":\"\",\"type\":null,\"exec\":{\"command\":[]},\"httpGet\":{\"host\":\"\",\"path\":\"\",\"port\":\"\",\"scheme\":\"\",\"httpHeaders\":[]},\"tcpSocket\":{\"host\":\"\",\"port\":\"\"}},\"StartupProbe\":{\"failureThreshold\":\"\",\"initialDelaySeconds\":\"\",\"periodSeconds\":\"\",\"successThreshold\":\"\",\"timeoutSeconds\":\"\",\"type\":null,\"exec\":{\"command\":[]},\"httpGet\":{\"host\":\"\",\"path\":\"\",\"port\":\"\",\"scheme\":\"\",\"httpHeaders\":[]},\"tcpSocket\":{\"host\":\"\",\"port\":\"\"}},\"Command\":[],\"Args\":[],\"InitContainer\":\"False\",\"SecurityContext\":{\"allowPrivilegeEscalation\":\"\",\"privileged\":\"\",\"readOnlyRootFilesystem\":\"\",\"runAsGroup\":\"\",\"runAsNonRoot\":\"\",\"runAsUser\":\"\",\"capabilities\":{\"add\":[],\"drop\":[]},\"seLinuxOptions\":{\"level\":\"\",\"role\":\"\",\"type\":\"\",\"user\":\"\"}},\"Lifecycle\":{},\"CreateFeedSecrets\":\"False\"}]",
        "Octopus.Action.KubernetesContainers.DeploymentAnnotations" : "[]",
        "Octopus.Action.KubernetesContainers.DeploymentLabels" : "{\"app\" : \"backend\"}",
        "Octopus.Action.KubernetesContainers.DeploymentName" : "backend",
        "Octopus.Action.KubernetesContainers.DeploymentResourceType" : "Deployment",
        "Octopus.Action.KubernetesContainers.DeploymentStyle" : "RollingUpdate",
        "Octopus.Action.KubernetesContainers.DeploymentWait" : "Wait",
        "Octopus.Action.KubernetesContainers.DnsConfigOptions" : "[]",
        "Octopus.Action.KubernetesContainers.IngressAnnotations" : "[{\"key\":\"alb.ingress.kubernetes.io/scheme\",\"keyError\":null,\"value\":\"internet-facing\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null},{\"key\":\"alb.ingress.kubernetes.io/healthcheck-path\",\"keyError\":null,\"value\":\"/health/customers/GET\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null},{\"key\":\"alb.ingress.kubernetes.io/target-type\",\"keyError\":null,\"value\":\"ip\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null},{\"key\":\"kubernetes.io/ingress.class\",\"keyError\":null,\"value\":\"alb\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null},{\"key\":\"alb.ingress.kubernetes.io/group.name\",\"keyError\":null,\"value\":\"octopub\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null}]",
        "Octopus.Action.KubernetesContainers.NodeAffinity" : "[]",
        "Octopus.Action.KubernetesContainers.PersistentVolumeClaims" : "[]",
        "Octopus.Action.KubernetesContainers.PodAffinity" : "[]",
        "Octopus.Action.KubernetesContainers.PodAnnotations" : "[]",
        "Octopus.Action.KubernetesContainers.PodAntiAffinity" : "[]",
        "Octopus.Action.KubernetesContainers.PodSecuritySysctls" : "[]",
        "Octopus.Action.KubernetesContainers.Replicas" : "1",
        "Octopus.Action.KubernetesContainers.RevisionHistoryLimit" : "1",
        "Octopus.Action.KubernetesContainers.ServiceNameType" : "External",
        "Octopus.Action.KubernetesContainers.ServiceType" : "ClusterIP",
        "Octopus.Action.KubernetesContainers.Tolerations" : "[]",
        "OctopusUseBundledTooling" : "False",
        "Octopus.Action.KubernetesContainers.PodManagementPolicy" : "OrderedReady",
        "Octopus.Action.KubernetesContainers.IngressName" : local.backend_ingress_name,
        "Octopus.Action.KubernetesContainers.IngressRules" : "[{\"host\":\"\",\"http\":{\"paths\":[{\"key\":\"/api/products\",\"value\":\"web\",\"option\":\"\",\"option2\":\"Prefix\"}]}},{\"host\":\"\",\"http\":{\"paths\":[{\"key\":\"/health/products\",\"value\":\"web\",\"option\":\"\",\"option2\":\"Prefix\"}]}}]",
        "Octopus.Action.KubernetesContainers.ServiceName" : local.backend_service_name,
        "Octopus.Action.KubernetesContainers.ServicePorts" : "[{\"name\":\"web\",\"port\":\"80\",\"targetPort\":\"8083\",\"nodePort\":\"\",\"protocol\":\"TCP\"}]"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Display the Ingress URL"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = ["Kubernetes Backend"]
    action {
      action_type    = "Octopus.KubernetesRunScript"
      name           = "Display the Ingress URL"
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]
      container {
        feed_id = var.octopus_dockerhub_feed_id
        image   = "octopusdeploy/worker-tools:3-ubuntu.18.04"
      }
      properties = {
        "Octopus.Action.Script.ScriptSource" : "Inline"
        "Octopus.Action.Script.Syntax" : "Bash"
        "Octopus.Action.Script.ScriptBody" : <<-EOT
          DNSNAME=$(kubectl get ingress ${local.backend_ingress_name} -o json | jq -r '.status.loadBalancer.ingress[0].hostname')
          set_octopusvariable "DNSName" "$${DNSNAME}"
          echo "Open [http://$DNSNAME/api/products](http://$DNSNAME/api/products) to view the backend API."
        EOT
        "OctopusUseBundledTooling" : "False"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "HTTP Smoke Test"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    run_script_action {
      can_be_used_for_project_versioning = false
      condition                          = "Success"
      is_disabled                        = false
      is_required                        = true
      script_syntax                      = "Bash"
      script_source                      = "Inline"
      run_on_server                      = true
      worker_pool_id                     = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      name                               = "HTTP Smoke Test"
      notes                              = "Use curl to perform a smoke test of a HTTP endpoint."
      environments                       = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]
      script_body = <<-EOT
          CODE=$(curl -o /dev/null -s -w "%%{http_code}\n" http://#{Octopus.Action[Display the Ingress URL].Output.DNSName}/health/products/GET)

          echo "response code: $${CODE}"
          if [ "$${CODE}" == "200" ]
          then
            echo "success"
            exit 0;
          else
            echo "error"
            exit 1;
          fi
        EOT
    }
  }
  step {
    condition           = "Success"
    name                = "Postman Integration Test"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    run_script_action {
      can_be_used_for_project_versioning = false
      condition                          = "Success"
      is_disabled                        = false
      is_required                        = true
      script_syntax                      = "Bash"
      script_source                      = "Inline"
      run_on_server                      = true
      worker_pool_id                     = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      name                               = "Postman Integration Test"
      notes                              = "Use curl to perform a smoke test of a HTTP endpoint."
      environments                       = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]
      features = ["Octopus.Features.JsonConfigurationVariables"]
      container {
        feed_id = var.octopus_k8s_feed_id
        image   = var.postman_docker_image
      }
      package {
        name                      = "products-microservice-postman"
        package_id                = "products-microservice-postman"
        feed_id                   = var.octopus_built_in_feed_id
        acquisition_location      = "Server"
        extract_during_deployment = true
      }
      properties = {
        "Octopus.Action.Package.JsonConfigurationVariablesTargets": "**/*.json"
      }
      script_body = <<-EOT
          newman run products-microservice-postman/test.json 2>&1
        EOT
    }
  }
  step {
    condition           = "Success"
    name                = "Check for Vulnerabilities"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    run_script_action {
      can_be_used_for_project_versioning = false
      condition                          = "Success"
      is_disabled                        = false
      is_required                        = true
      script_syntax                      = "Bash"
      script_source                      = "Inline"
      run_on_server                      = true
      worker_pool_id                     = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      name                               = "Check for Vulnerabilities"
      environments                       = [
        data.octopusdeploy_environments.development_security.environments[0].id,
        data.octopusdeploy_environments.production_security.environments[0].id
      ]
      package {
        name                      = "products-microservice-sbom"
        package_id                = "products-microservice-sbom"
        feed_id                   = var.octopus_built_in_feed_id
        acquisition_location      = "Server"
        extract_during_deployment = true
      }
      script_body = local.vulnerability_scan
    }
  }
}