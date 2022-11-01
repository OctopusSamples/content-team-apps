resource "octopusdeploy_project" "deploy_frontend_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys the frontend service."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_application_lifecycle_id
  name                                 = "Frontend WebApp"
  project_group_id                     = octopusdeploy_project_group.frontend_project_group.id
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

resource "octopusdeploy_variable" "frontend_debug_variable" {
  name         = "OctopusPrintVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_frontend_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "frontend_debug_evaluated_variable" {
  name         = "OctopusPrintEvaluatedVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_frontend_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "cypress_baseurl_variable" {
  name         = "baseUrl"
  type         = "String"
  description  = "A structured variable replacement for the Cypress test."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_frontend_project.id
  value        = "http://#{Octopus.Action[Display the Ingress URL].Output.DNSName}"
}

locals {
  frontend_package_id = "frontend"
  frontend_service_name = "frontend-service"
  frontend_ingress_name = "frontend-ingress"
}

resource "octopusdeploy_deployment_process" "deploy_frontend" {
  project_id = octopusdeploy_project.deploy_frontend_project.id
  step {
    condition           = "Success"
    name                = "Frontend WebApp"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = ["Kubernetes Frontend"]
    action {
      action_type    = "Octopus.KubernetesDeployContainers"
      name           = "Frontend WebApp"
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]
      features = ["Octopus.Features.KubernetesService", "Octopus.Features.KubernetesIngress", "Octopus.Features.KubernetesConfigMap"]
      package {
        name                      = local.frontend_package_id
        package_id                = var.frontend_docker_image
        feed_id                   = var.octopus_k8s_feed_id
        acquisition_location      = "NotAcquired"
        extract_during_deployment = false
      }
      container {
        feed_id = var.octopus_dockerhub_feed_id
        image   = "octopusdeploy/worker-tools:3-ubuntu.18.04"
      }
      properties = {
        "Octopus.Action.KubernetesContainers.CombinedVolumes": "[{\"Items\":[{\"key\":\"config.json\",\"keyError\":null,\"value\":\"config.json\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null}],\"Name\":\"frontend-config-volume\",\"ReferenceName\":\"\",\"ReferenceNameType\":\"LinkedResource\",\"EmptyDirMedium\":\"\",\"HostPathType\":\"Directory\",\"HostPathPath\":\"\",\"LocalPath\":\"\",\"Type\":\"ConfigMap\",\"RawYaml\":\"\",\"Repository\":\"\",\"Revision\":\"\"}]",
        "Octopus.Action.KubernetesContainers.Containers" : "[{\"Name\":\"${local.frontend_package_id}\",\"Ports\":[{\"key\":\"web\",\"keyError\":null,\"value\":\"5000\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null}],\"EnvironmentVariables\":[],\"SecretEnvironmentVariables\":[],\"ConfigMapEnvironmentVariables\":[],\"FieldRefEnvironmentVariables\":[],\"ConfigMapEnvFromSource\":[],\"SecretEnvFromSource\":[],\"VolumeMounts\":[{\"key\":\"frontend-config-volume\",\"keyError\":null,\"value\":\"/workspace/build/config.json\",\"valueError\":null,\"option\":\"config.json\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null}],\"Resources\":{\"requests\":{\"memory\":\"256Mi\",\"cpu\":\"\",\"ephemeralStorage\":\"\"},\"limits\":{\"memory\":\"1Gi\",\"cpu\":\"\",\"ephemeralStorage\":\"\",\"nvidiaGpu\":\"\",\"amdGpu\":\"\"}},\"LivenessProbe\":{\"failureThreshold\":\"\",\"initialDelaySeconds\":\"\",\"periodSeconds\":\"\",\"successThreshold\":\"\",\"timeoutSeconds\":\"\",\"type\":null,\"exec\":{\"command\":[]},\"httpGet\":{\"host\":\"\",\"path\":\"\",\"port\":\"\",\"scheme\":\"\",\"httpHeaders\":[]},\"tcpSocket\":{\"host\":\"\",\"port\":\"\"}},\"ReadinessProbe\":{\"failureThreshold\":\"\",\"initialDelaySeconds\":\"\",\"periodSeconds\":\"\",\"successThreshold\":\"\",\"timeoutSeconds\":\"\",\"type\":null,\"exec\":{\"command\":[]},\"httpGet\":{\"host\":\"\",\"path\":\"\",\"port\":\"\",\"scheme\":\"\",\"httpHeaders\":[]},\"tcpSocket\":{\"host\":\"\",\"port\":\"\"}},\"StartupProbe\":{\"failureThreshold\":\"\",\"initialDelaySeconds\":\"\",\"periodSeconds\":\"\",\"successThreshold\":\"\",\"timeoutSeconds\":\"\",\"type\":null,\"exec\":{\"command\":[]},\"httpGet\":{\"host\":\"\",\"path\":\"\",\"port\":\"\",\"scheme\":\"\",\"httpHeaders\":[]},\"tcpSocket\":{\"host\":\"\",\"port\":\"\"}},\"Command\":[],\"Args\":[],\"InitContainer\":\"False\",\"SecurityContext\":{\"allowPrivilegeEscalation\":\"\",\"privileged\":\"\",\"readOnlyRootFilesystem\":\"\",\"runAsGroup\":\"\",\"runAsNonRoot\":\"\",\"runAsUser\":\"\",\"capabilities\":{\"add\":[],\"drop\":[]},\"seLinuxOptions\":{\"level\":\"\",\"role\":\"\",\"type\":\"\",\"user\":\"\"}},\"Lifecycle\":{},\"CreateFeedSecrets\":\"False\"}]",
        "Octopus.Action.KubernetesContainers.DeploymentAnnotations" : "[]",
        "Octopus.Action.KubernetesContainers.DeploymentLabels" : "{\"app\" : \"frontend\"}",
        "Octopus.Action.KubernetesContainers.DeploymentName" : "frontend",
        "Octopus.Action.KubernetesContainers.DeploymentResourceType" : "Deployment",
        "Octopus.Action.KubernetesContainers.DeploymentStyle" : "RollingUpdate",
        "Octopus.Action.KubernetesContainers.DeploymentWait" : "Wait",
        "Octopus.Action.KubernetesContainers.DnsConfigOptions" : "[]",
        "Octopus.Action.KubernetesContainers.IngressAnnotations" : "[{\"key\":\"alb.ingress.kubernetes.io/group.order\",\"keyError\":null,\"value\":\"500\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null},{\"key\":\"alb.ingress.kubernetes.io/scheme\",\"keyError\":null,\"value\":\"internet-facing\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null},{\"key\":\"alb.ingress.kubernetes.io/healthcheck-path\",\"keyError\":null,\"value\":\"/\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null},{\"key\":\"alb.ingress.kubernetes.io/target-type\",\"keyError\":null,\"value\":\"ip\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null},{\"key\":\"kubernetes.io/ingress.class\",\"keyError\":null,\"value\":\"alb\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null},{\"key\":\"alb.ingress.kubernetes.io/group.name\",\"keyError\":null,\"value\":\"octopub\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null}]",
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
        "Octopus.Action.KubernetesContainers.IngressName" : local.frontend_ingress_name,
        "Octopus.Action.KubernetesContainers.IngressRules" : "[{\"host\":\"\",\"http\":{\"paths\":[{\"key\":\"/*\",\"value\":\"web\",\"option\":\"\",\"option2\":\"ImplementationSpecific\"}]}}]",
        "Octopus.Action.KubernetesContainers.ServiceName" : local.frontend_service_name,
        "Octopus.Action.KubernetesContainers.ServicePorts" : "[{\"name\":\"web\",\"port\":\"80\",\"targetPort\":\"5000\",\"nodePort\":\"\",\"protocol\":\"TCP\"}]",
        "Octopus.Action.KubernetesContainers.ConfigMapName": "frontend-config",
        "Octopus.Action.KubernetesContainers.ConfigMapValues": "{\"config.json\":\"{\\n  \\\"basename\\\": \\\"\\\",\\n  \\\"branch\\\": \\\"main\\\",\\n  \\\"title\\\": \\\"Octopub\\\",\\n  \\\"productEndpoint\\\": \\\"/api/products\\\",\\n  \\\"productHealthEndpoint\\\": \\\"/health/products\\\",\\n  \\\"auditEndpoint\\\": \\\"/api/audits\\\",\\n  \\\"auditHealthEndpoint\\\": \\\"/health/audits\\\"\\n}\"}"
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Display the Ingress URL"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = ["Kubernetes Frontend"]
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
          # It can take a while for a load balancer to be provisioned
          for i in {1..60}
          do
              DNSNAME=$(kubectl get ingress ${local.frontend_ingress_name} -o json | jq -r '.status.loadBalancer.ingress[0].hostname')
              if [[ "$${DNSNAME}" != "null" ]]
              then
                break
              fi
              echo "Waiting for Ingress hostname"
              sleep 10
          done
          set_octopusvariable "DNSName" "$${DNSNAME}"

          if [[ "$${DNSNAME}" != "null" ]]
          then
            write_highlight "Open [http://$DNSNAME/index.html](http://$DNSNAME/index.html) to view the web app."
          fi
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
          if [[ "#{Octopus.Action[Display the Ingress URL].Output.DNSName}" == "null" ]]
          then
            echo "The previous step failed to find the ingress hostname. This means we are unable to test the service."
            exit 1
          fi

          # Load balancers can take a minute or so before their DNS is propagated.
          # A status code of 000 means curl could not resolve the DNS name, so we wait for a bit until DNS is updated.
          for i in {1..30}
          do
              CODE=$(curl -o /dev/null -s -w "%%{http_code}\n" http://#{Octopus.Action[Display the Ingress URL].Output.DNSName}/index.html)
              if [[ "$${CODE}" == "200" ]]
              then
                break
              fi
              echo "Waiting for DNS name to be resolvable and for service to respond"
              sleep 10
          done

          echo "response code: $${CODE}"
          if [[ "$${CODE}" == "200" ]]
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
    name                = "Cypress E2E Test"
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
      name                               = "Cypress E2E Test"
      notes                              = "Use cypress to perform an end to end test of the frontend web app."
      environments                       = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]
      features = ["Octopus.Features.JsonConfigurationVariables"]
      container {
        feed_id = var.octopus_k8s_feed_id
        image   = var.cypress_docker_image
      }
      package {
        name                      = "octopub-frontend-cypress"
        package_id                = "octopub-frontend-cypress"
        feed_id                   = var.octopus_built_in_feed_id
        acquisition_location      = "Server"
        extract_during_deployment = true
      }
      properties = {
        "Octopus.Action.Package.JsonConfigurationVariablesTargets": "**/cypress.json"
      }
      script_body = <<-EOT
          if [[ "#{Octopus.Action[Display the Ingress URL].Output.DNSName}" == "null" ]]
          then
            echo "The previous step failed to find the ingress hostname. This means we are unable to test the service."
            exit 1
          fi

          echo "##octopus[stdout-verbose]"
          cd octopub-frontend-cypress
          OUTPUT=$(cypress run 2>&1)
          RESULT=$?
          echo "##octopus[stdout-default]"

          # Print the output stripped of ANSI colour codes
          echo -e "$${OUTPUT}" | sed 's/\x1b\[[0-9;]*m//g'

          if [[ -f mochawesome.html ]]
          then
            inline-assets mochawesome.html selfcontained.html
            new_octopusartifact "$${PWD}/selfcontained.html" "selfcontained.html"
          fi
          if [[ -d cypress/screenshots/sample_spec.js ]]
          then
            zip -r screenshots.zip cypress/screenshots/sample_spec.js
            new_octopusartifact "$${PWD}/screenshots.zip" "screenshots.zip"
          fi
          exit $${RESULT}
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
        name                      = "javascript-frontend-sbom"
        package_id                = "javascript-frontend-sbom"
        feed_id                   = var.octopus_built_in_feed_id
        acquisition_location      = "Server"
        extract_during_deployment = true
      }
      script_body = local.vulnerability_scan
    }
  }
}