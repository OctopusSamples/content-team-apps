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
  name                                 = "Deploy Frontend WebApp"
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


locals {
  frontend_package_name = "frontend"
  frontend_service_name = "frontend-service"
  frontend_ingress_name = "frontend-ingress"
}

resource "octopusdeploy_deployment_process" "deploy_frontend_backend" {
  project_id = octopusdeploy_project.deploy_frontend_project.id
  step {
    condition           = "Success"
    name                = "Deploy Frontend WebApp"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = ["Kubernetes Frontend"]
    action {
      action_type    = "Octopus.KubernetesDeployContainers"
      name           = "Deploy Frontend WebApp"
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      environments   = [
        data.octopusdeploy_environments.development.environments[0].id,
        data.octopusdeploy_environments.production.environments[0].id
      ]
      features = ["Octopus.Features.KubernetesService", "Octopus.Features.KubernetesIngress", "Octopus.Features.KubernetesConfigMap"]
      package {
        name                      = local.frontend_package_name
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
        "Octopus.Action.KubernetesContainers.Containers" : "[{\"Name\":\"${local.frontend_package_name}\",\"Ports\":[{\"key\":\"web\",\"keyError\":null,\"value\":\"5000\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null}],\"EnvironmentVariables\":[],\"SecretEnvironmentVariables\":[],\"ConfigMapEnvironmentVariables\":[],\"FieldRefEnvironmentVariables\":[],\"ConfigMapEnvFromSource\":[],\"SecretEnvFromSource\":[],\"VolumeMounts\":[{\"key\":\"frontend-config-volume\",\"keyError\":null,\"value\":\"/workspace/build/config.json\",\"valueError\":null,\"option\":\"config.json\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null}],\"Resources\":{\"requests\":{\"memory\":\"256Mi\",\"cpu\":\"\",\"ephemeralStorage\":\"\"},\"limits\":{\"memory\":\"1Gi\",\"cpu\":\"\",\"ephemeralStorage\":\"\",\"nvidiaGpu\":\"\",\"amdGpu\":\"\"}},\"LivenessProbe\":{\"failureThreshold\":\"\",\"initialDelaySeconds\":\"\",\"periodSeconds\":\"\",\"successThreshold\":\"\",\"timeoutSeconds\":\"\",\"type\":null,\"exec\":{\"command\":[]},\"httpGet\":{\"host\":\"\",\"path\":\"\",\"port\":\"\",\"scheme\":\"\",\"httpHeaders\":[]},\"tcpSocket\":{\"host\":\"\",\"port\":\"\"}},\"ReadinessProbe\":{\"failureThreshold\":\"\",\"initialDelaySeconds\":\"\",\"periodSeconds\":\"\",\"successThreshold\":\"\",\"timeoutSeconds\":\"\",\"type\":null,\"exec\":{\"command\":[]},\"httpGet\":{\"host\":\"\",\"path\":\"\",\"port\":\"\",\"scheme\":\"\",\"httpHeaders\":[]},\"tcpSocket\":{\"host\":\"\",\"port\":\"\"}},\"StartupProbe\":{\"failureThreshold\":\"\",\"initialDelaySeconds\":\"\",\"periodSeconds\":\"\",\"successThreshold\":\"\",\"timeoutSeconds\":\"\",\"type\":null,\"exec\":{\"command\":[]},\"httpGet\":{\"host\":\"\",\"path\":\"\",\"port\":\"\",\"scheme\":\"\",\"httpHeaders\":[]},\"tcpSocket\":{\"host\":\"\",\"port\":\"\"}},\"Command\":[],\"Args\":[],\"InitContainer\":\"False\",\"SecurityContext\":{\"allowPrivilegeEscalation\":\"\",\"privileged\":\"\",\"readOnlyRootFilesystem\":\"\",\"runAsGroup\":\"\",\"runAsNonRoot\":\"\",\"runAsUser\":\"\",\"capabilities\":{\"add\":[],\"drop\":[]},\"seLinuxOptions\":{\"level\":\"\",\"role\":\"\",\"type\":\"\",\"user\":\"\"}},\"Lifecycle\":{},\"CreateFeedSecrets\":\"False\"}]",
        "Octopus.Action.KubernetesContainers.DeploymentAnnotations" : "[]",
        "Octopus.Action.KubernetesContainers.DeploymentLabels" : "{\"app\" : \"frontend\"}",
        "Octopus.Action.KubernetesContainers.DeploymentName" : "frontend",
        "Octopus.Action.KubernetesContainers.DeploymentResourceType" : "Deployment",
        "Octopus.Action.KubernetesContainers.DeploymentStyle" : "RollingUpdate",
        "Octopus.Action.KubernetesContainers.DeploymentWait" : "Wait",
        "Octopus.Action.KubernetesContainers.DnsConfigOptions" : "[]",
        "Octopus.Action.KubernetesContainers.IngressAnnotations" : "[{\"key\":\"alb.ingress.kubernetes.io/scheme\",\"keyError\":null,\"value\":\"internet-facing\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null},{\"key\":\"alb.ingress.kubernetes.io/healthcheck-path\",\"keyError\":null,\"value\":\"/\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null},{\"key\":\"alb.ingress.kubernetes.io/target-type\",\"keyError\":null,\"value\":\"ip\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null},{\"key\":\"kubernetes.io/ingress.class\",\"keyError\":null,\"value\":\"alb\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null},{\"key\":\"alb.ingress.kubernetes.io/group.name\",\"keyError\":null,\"value\":\"octopub\",\"valueError\":null,\"option\":\"\",\"optionError\":null,\"option2\":\"\",\"option2Error\":null}]",
        "Octopus.Action.KubernetesContainers.NodeAffinity" : "[]",
        "Octopus.Action.KubernetesContainers.PersistentVolumeClaims" : "[]",
        "Octopus.Action.KubernetesContainers.PodAffinity" : "[]",
        "Octopus.Action.KubernetesContainers.PodAnnotations" : "[]",
        "Octopus.Action.KubernetesContainers.PodAntiAffinity" : "[]",
        "Octopus.Action.KubernetesContainers.PodSecuritySysctls" : "[]",
        "Octopus.Action.KubernetesContainers.Replicas" : "1",
        "Octopus.Action.KubernetesContainers.RevisionHistoryLimit" : "1",
        "Octopus.Action.KubernetesContainers.ServiceNameType" : "External",
        "Octopus.Action.KubernetesContainers.ServiceType" : "LoadBalancer",
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
          DNSNAME=$(kubectl get ingress ${local.frontend_ingress_name} -o json | jq -r '.status.loadBalancer.ingress[0].hostname')
          set_octopusvariable "DNSName" "$${DNSNAME}"
          echo "Open [http://$DNSNAME/index.html](http://$DNSNAME/index.html) to view the web app."
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
          CODE=$(curl -o /dev/null -s -w "%%{http_code}\n" http://#{Octopus.Action[Display the Ingress URL].Output.FixedEnvironment}/index.html)

          echo "response code:$code"
          if [ "$code" == "200" ]
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
      script_body = <<-EOT
        TIMESTAMP=$(date +%s%3N)
        SUCCESS=0
        for x in $(find . -name bom.xml -type f -print); do
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

            # Report file is not generated if no threats found
            # https://github.com/ShiftLeftSecurity/sast-scan/issues/168
            if [[ -f "$PWD/depscan-bom.json" ]]; then
              new_octopusartifact "$PWD/depscan-bom.json"
              # The number of lines in the report file equals the number of vulnerabilities found
              COUNT=$(wc -l < "$PWD/depscan-bom.json")
            else
              COUNT=0
            fi

            # Print the output stripped of ANSI colour codes
            echo -e "$${OUTPUT}" | sed 's/\x1b\[[0-9;]*m//g'
        done

        set_octopusvariable "VerificationResult" $SUCCESS

        if [[  $SUCCESS -ne 0 ]]; then
          >&2 echo "Vulnerabilities were detected"
        fi

        exit 0
        EOT
    }
  }
}