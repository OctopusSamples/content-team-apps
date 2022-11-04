locals {
  frontend_package_id        = "octopussamples/frontend"
  frontend_package_name       = "frontend"
  frontend_resource_names      = "frontend#{unless Octopus.Release.Channel.Name == \"Mainline\"}-#{Octopus.Release.Channel.Name}#{/unless}"
  frontend_project_name        = "Frontend"
  frontend_project_description = "Deploys the frontend web app."
  frontend_containers          = jsonencode([
    {
      IsNew : true
      InitContainer : "False"
      Ports : [
        {
          value : "8080"
        }
      ]
      EnvironmentVariables : [
        {
          key : "PORT"
          value : "8080"
        },
        {
          key : "PRODUCT_CATALOG_SERVICE_ADDR"
          value : "productcatalogservice:3550"
        },
        {
          key : "CURRENCY_SERVICE_ADDR"
          value : "currencyservice:7000"
        },
        {
          key : "CART_SERVICE_ADDR"
          value : "cartservice:7070"
        },
        {
          key : "RECOMMENDATION_SERVICE_ADDR"
          value : "recommendationservice:8080"
        },
        {
          key : "SHIPPING_SERVICE_ADDR"
          value : "shippingservice:50051"
        },
        {
          key : "CHECKOUT_SERVICE_ADDR"
          value : "checkoutservice:5050"
        },
        {
          key : "AD_SERVICE_ADDR"
          value : "adservice:9555"
        },
        {
          key : "DISABLE_TRACING"
          value : "1"
        },
        {
          key : "DISABLE_PROFILER"
          value : "1"
        }
      ]
      SecretEnvironmentVariables : []
      SecretEnvFromSource : []
      ConfigMapEnvironmentVariables : []
      ConfigMapEnvFromSource : []
      FieldRefEnvironmentVariables : []
      VolumeMounts : []
      AcquisitionLocation : "NotAcquired"
      Name : local.frontend_package_name
      PackageId : local.frontend_package_id
      FeedId : var.octopus_dockerhub_feed_id
      Properties : {}
      Command : []
      Args : []
      Resources : {
        requests : {
          memory : "64Mi"
          cpu : "100m"
          ephemeralStorage : ""
        }
        limits : {
          memory : "128Mi"
          cpu : "200m"
          ephemeralStorage : ""
          nvidiaGpu : ""
          amdGpu : ""
        }
      }
      LivenessProbe : {
        failureThreshold : ""
        initialDelaySeconds : "10"
        periodSeconds : ""
        successThreshold : ""
        timeoutSeconds : ""
        type : "HttpGet"
        exec : {
          command : []
        }
        httpGet : {
          host : ""
          path : "/_healthz"
          port : "8080"
          scheme : ""
          httpHeaders : [
            {
              key : "Cookie"
              value : "shop_session-id=x-liveness-probe"
            }
          ]
        }
        tcpSocket : {
          host : ""
          port : ""
        }
      }
      ReadinessProbe : {
        failureThreshold : ""
        initialDelaySeconds : "10"
        periodSeconds : ""
        successThreshold : ""
        timeoutSeconds : ""
        type : "HttpGet"
        exec : {
          command : []
        }
        httpGet : {
          host : ""
          path : "/_healthz"
          port : "8080"
          scheme : ""
          httpHeaders : [
            {
              key : "Cookie"
              value : "shop_session-id=x-readiness-probe"
            }
          ]
        }
        tcpSocket : {
          host : ""
          port : ""
        }
      }
      StartupProbe : {
        failureThreshold : ""
        initialDelaySeconds : ""
        periodSeconds : ""
        successThreshold : ""
        timeoutSeconds : ""
        type : null
        exec : {
          command : []
        }
        httpGet : {
          host : ""
          path : ""
          port : ""
          scheme : ""
          httpHeaders : []
        }
        tcpSocket : {
          host : ""
          port : ""
        }
      }
      Lifecycle : {}
      SecurityContext : {
        allowPrivilegeEscalation : "false"
        privileged : "false"
        readOnlyRootFilesystem : "true"
        runAsGroup : ""
        runAsNonRoot : ""
        runAsUser : ""
        capabilities : {
          add : []
          drop : ["all"]
        }
        seLinuxOptions : {
          level : ""
          role : ""
          type : ""
          user : ""
        }
      }
    }
  ])
}

resource "octopusdeploy_project" "frontend_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = local.frontend_project_description
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_application_lifecycle_id
  name                                 = local.frontend_project_name
  project_group_id                     = octopusdeploy_project_group.google_microservice_demo.id
  tenanted_deployment_participation    = "Untenanted"
  space_id                             = var.octopus_space_id
  included_library_variable_sets       = []
  versioning_strategy {
    template = local.versioning_strategy
  }

  connectivity_policy {
    allow_deployments_to_no_targets = false
    exclude_unhealthy_targets       = false
    skip_machine_behavior           = "SkipUnavailableMachines"
  }
}

resource "octopusdeploy_channel" "frontend_feature_branch" {
  name        = "Feature Branches"
  project_id  = octopusdeploy_project.frontend_project.id
  description = "The channel through which feature branches are deployed"
  depends_on  = [octopusdeploy_deployment_process.deploy_frontend]
  is_default  = false
  rule {
    tag = ".+"
    action_package {
      deployment_action = local.deployment_step
      package_reference = local.frontend_package_name
    }
  }
}

resource "octopusdeploy_channel" "frontend_mainline" {
  name        = "Mainline"
  project_id  = octopusdeploy_project.frontend_project.id
  description = "The channel through which mainline releases are deployed"
  depends_on  = [octopusdeploy_deployment_process.deploy_frontend]
  is_default  = true
  rule {
    tag = "^$"
    action_package {
      deployment_action = local.deployment_step
      package_reference = local.frontend_package_name
    }
  }
}

resource "octopusdeploy_variable" "frontend_debug_variable" {
  name         = "OctopusPrintVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.frontend_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "frontend_debug_evaluated_variable" {
  name         = "OctopusPrintEvaluatedVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.frontend_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "frontend_mainline_namespace" {
  name         = "Namespace"
  type         = "String"
  description  = "The namespace used to deploy the mainline resources. These are placed in environment specific namespaces."
  is_sensitive = false
  owner_id     = octopusdeploy_project.frontend_project.id
  value        = local.namespace
  scope {
    environments = [
      var.octopus_development_app_environment_id,
      var.octopus_production_app_environment_id
    ]
  }
}

resource "octopusdeploy_variable" "frontend_featurebranch_namespace" {
  name         = "Namespace"
  type         = "String"
  description  = "The namespace used to deploy the feature branch resources. These are placed in the development namespace."
  is_sensitive = false
  owner_id     = octopusdeploy_project.frontend_project.id
  value        = local.feature_branch_namespace
}

resource "octopusdeploy_deployment_process" "deploy_frontend" {
  project_id = octopusdeploy_project.frontend_project.id
  step {
    condition           = "Success"
    name                = local.deployment_step
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = [local.deployment_role]
    action {
      action_type           = "Octopus.KubernetesDeployContainers"
      name                  = local.deployment_step
      run_on_server         = true
      worker_pool_id        = local.worker_pool_id
      excluded_environments = [
        var.octopus_development_security_environment_id,
        var.octopus_production_security_environment_id
      ]
      environments = []
      features = ["Octopus.Features.KubernetesService"]
      package {
        name                      = local.frontend_package_name
        package_id                = local.frontend_package_id
        feed_id                   = var.octopus_dockerhub_feed_id
        acquisition_location      = "NotAcquired"
        extract_during_deployment = false
      }
      container {
        feed_id = var.octopus_dockerhub_feed_id
        image   = local.worker_image
      }
      properties = {
        "Octopus.Action.KubernetesContainers.Replicas" : "1",
        "Octopus.Action.KubernetesContainers.DeploymentStyle" : "RollingUpdate",
        "Octopus.Action.KubernetesContainers.ServiceNameType" : "External",
        "Octopus.Action.KubernetesContainers.DeploymentResourceType" : "Deployment",
        "Octopus.Action.KubernetesContainers.DeploymentWait" : "Wait",
        "Octopus.Action.KubernetesContainers.ServiceType" : "ClusterIP",
        "Octopus.Action.KubernetesContainers.IngressAnnotations" : "[]",
        "Octopus.Action.KubernetesContainers.PersistentVolumeClaims" : "[]",
        "Octopus.Action.KubernetesContainers.Tolerations" : "[]",
        "Octopus.Action.KubernetesContainers.NodeAffinity" : "[]",
        "Octopus.Action.KubernetesContainers.PodAffinity" : "[]",
        "Octopus.Action.KubernetesContainers.PodAntiAffinity" : "[]",
        "Octopus.Action.KubernetesContainers.Namespace" : "#{Namespace}",
        "Octopus.Action.KubernetesContainers.DeploymentName" : local.frontend_resource_names,
        "Octopus.Action.KubernetesContainers.DnsConfigOptions" : "[]",
        "Octopus.Action.KubernetesContainers.PodAnnotations" : "[{\"key\":\"sidecar.istio.io/rewriteAppHTTPProbers\",\"value\":\"true\"}]",
        "Octopus.Action.KubernetesContainers.DeploymentAnnotations" : "[]",
        "Octopus.Action.KubernetesContainers.DeploymentLabels" : "{\"app\":\"${local.frontend_package_name}\"}",
        "Octopus.Action.KubernetesContainers.CombinedVolumes" : "[]",
        "Octopus.Action.KubernetesContainers.PodSecurityFsGroup" : "1000",
        "Octopus.Action.KubernetesContainers.PodSecurityRunAsGroup" : "1000",
        "Octopus.Action.KubernetesContainers.PodSecurityRunAsNonRoot" : "true",
        "Octopus.Action.KubernetesContainers.PodSecuritySysctls" : "[]",
        "Octopus.Action.KubernetesContainers.PodServiceAccountName" : "default",
        "Octopus.Action.KubernetesContainers.Containers" : local.frontend_containers,
        "Octopus.Action.KubernetesContainers.PodSecurityRunAsUser" : "1000",
        "Octopus.Action.KubernetesContainers.ServiceName" : local.frontend_resource_names,
        "Octopus.Action.KubernetesContainers.LoadBalancerAnnotations" : "[]",
        "Octopus.Action.KubernetesContainers.ServicePorts" : jsonencode([
          {
            name : "http"
            port : "80"
            targetPort : "8080"
          }
        ])
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Expose Frontend via LoadBalancer"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = [local.deployment_role]
    action {
      action_type           = "Octopus.KubernetesDeployRawYaml"
      name                  = "Expose Frontend via LoadBalancer"
      run_on_server         = true
      worker_pool_id        = local.worker_pool_id
      excluded_environments = [
        var.octopus_development_security_environment_id,
        var.octopus_production_security_environment_id
      ]
      environments = []
      container {
        feed_id = var.octopus_dockerhub_feed_id
        image   = local.worker_image
      }
      properties = {
        "Octopus.Action.Script.ScriptSource": "Inline",
        "Octopus.Action.KubernetesContainers.CustomResourceYaml": <<EOF
apiVersion: v1
kind: Service
metadata:
  name: ${local.frontend_resource_names}-external
spec:
  type: LoadBalancer
  selector:
    app: ${local.frontend_package_name}
  ports:
  - name: http
    port: 80
    targetPort: 8080
EOF
        "Octopus.Action.KubernetesContainers.Namespace": "#{Namespace}",
      }
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
      worker_pool_id                     = local.worker_pool_id
      name                               = "Check for Vulnerabilities"
      notes                              = "Scans the SBOM for any known vulnerabilities."
      environments                       = [
        var.octopus_development_security_environment_id,
        var.octopus_production_security_environment_id
      ]
      package {
        name                      = "frontend-sbom"
        package_id                = "microservices-demo:frontend-sbom"
        feed_id                   = octopusdeploy_maven_feed.github_maven_feed.id
        acquisition_location      = "Server"
        extract_during_deployment = true
      }
      script_body = local.vulnerability_scan
    }
  }
}