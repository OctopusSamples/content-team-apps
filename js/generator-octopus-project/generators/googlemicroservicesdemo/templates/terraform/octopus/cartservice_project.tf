locals {
  cartservice_package_name        = "octopussamples/cartservice"
  cartservice_resource_names      = "cartservice"
  cartservice_project_name        = "Cart Service"
  cartservice_project_description = "Deploys the cart service."
  cartservice_service_ports       = "[{\"name\":\"grpc\",\"port\":\"7070\",\"targetPort\":\"7070\"}]"
  cartservice_containers          = jsonencode([
    {
      IsNew : true,
      InitContainer : "False",
      Ports : [
        {
          value : "7070"
        }
      ],
      EnvironmentVariables : [
        {
          key : "REDIS_ADDR",
          value : "redis-cart:6379"
        }
      ],
      SecretEnvironmentVariables : [],
      SecretEnvFromSource : [],
      ConfigMapEnvironmentVariables : [],
      ConfigMapEnvFromSource : [],
      FieldRefEnvironmentVariables : [],
      VolumeMounts : [],
      AcquisitionLocation : "NotAcquired",
      Name : local.cartservice_resource_names
      PackageId : local.cartservice_package_name
      FeedId : var.octopus_dockerhub_feed_id
      Properties : {

      },
      Command : [],
      Args : [],
      Resources : {
        requests : {
          memory : "64Mi",
          cpu : "200m",
          ephemeralStorage : ""
        },
        limits : {
          memory : "128Mi",
          cpu : "300m",
          ephemeralStorage : "",
          nvidiaGpu : "",
          amdGpu : ""
        }
      },
      LivenessProbe : {
        failureThreshold : "",
        initialDelaySeconds : "15",
        periodSeconds : "10",
        successThreshold : "",
        timeoutSeconds : "",
        type : "Command",
        exec : {
          command : [
            "/bin/grpc_health_probe",
            "-addr=:7070",
            "-rpc-timeout=5s"
          ]
        },
        httpGet : {
          host : "",
          path : "",
          port : "",
          scheme : "",
          httpHeaders : []
        },
        tcpSocket : {
          host : "",
          port : ""
        }
      },
      ReadinessProbe : {
        failureThreshold : "",
        initialDelaySeconds : "15",
        periodSeconds : "",
        successThreshold : "",
        timeoutSeconds : "",
        type : "Command",
        exec : {
          command : [
            "/bin/grpc_health_probe",
            "-addr=:7070",
            "-rpc-timeout=5s"
          ]
        },
        httpGet : {
          host : "",
          path : "",
          port : "",
          scheme : "",
          httpHeaders : []
        },
        tcpSocket : {
          host : "",
          port : ""
        }
      },
      StartupProbe : {
        failureThreshold : "",
        initialDelaySeconds : "",
        periodSeconds : "",
        successThreshold : "",
        timeoutSeconds : "",
        type : null,
        exec : {
          command : []
        },
        httpGet : {
          host : "",
          path : "",
          port : "",
          scheme : "",
          httpHeaders : []
        },
        tcpSocket : {
          host : "",
          port : ""
        }
      },
      Lifecycle : {

      },
      SecurityContext : {
        allowPrivilegeEscalation : "false",
        privileged : "false",
        readOnlyRootFilesystem : "true",
        runAsGroup : "",
        runAsNonRoot : "",
        runAsUser : "",
        capabilities : {
          add : [],
          drop : ["all"]
        },
        seLinuxOptions : {
          level : "",
          role : "",
          type : "",
          user : ""
        }
      }
    }
  ])
}

resource "octopusdeploy_project" "cartservice_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = local.cartservice_project_description
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_application_lifecycle_id
  name                                 = local.cartservice_project_name
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

resource "octopusdeploy_channel" "cartservice_feature_branch" {
  name        = "Feature Branches"
  project_id  = octopusdeploy_project.cartservice_project.id
  description = "The channel through which feature branches are deployed"
  depends_on  = [octopusdeploy_deployment_process.cartservice_deployment_process]
  is_default  = false
  rule {
    tag = ".+"
    action_package {
      deployment_action = local.deployment_step
      package_reference = local.cartservice_resource_names
    }
  }
}

resource "octopusdeploy_channel" "cartservice_mainline" {
  name        = "Mainline"
  project_id  = octopusdeploy_project.cartservice_project.id
  description = "The channel through which mainline releases are deployed"
  depends_on  = [octopusdeploy_deployment_process.cartservice_deployment_process]
  is_default  = true
  rule {
    tag = "^$"
    action_package {
      deployment_action = local.deployment_step
      package_reference = local.cartservice_resource_names
    }
  }
}

resource "octopusdeploy_variable" "cartservice_debug_variable" {
  name         = "OctopusPrintVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.cartservice_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "cartservice_debug_evaluated_variable" {
  name         = "OctopusPrintEvaluatedVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.cartservice_project.id
  value        = "False"
}

resource "octopusdeploy_deployment_process" "cartservice_deployment_process" {
  project_id = octopusdeploy_project.cartservice_project.id
  step {
    condition           = "Success"
    name                = local.deployment_step
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = [local.deployment_role]
    action {
      action_type    = "Octopus.KubernetesDeployContainers"
      name           = local.deployment_step
      run_on_server  = true
      worker_pool_id = local.worker_pool_id
      environments   = [
        var.octopus_development_app_environment_id,
        var.octopus_production_app_environment_id
      ]
      features = ["Octopus.Features.KubernetesService"]
      package {
        name                      = local.cartservice_resource_names
        package_id                = local.cartservice_package_name
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
        "Octopus.Action.KubernetesContainers.Namespace" : local.namespace,
        "Octopus.Action.KubernetesContainers.DeploymentName" : local.cartservice_resource_names,
        "Octopus.Action.KubernetesContainers.TerminationGracePeriodSeconds": "5",
        "Octopus.Action.KubernetesContainers.DnsConfigOptions" : "[]",
        "Octopus.Action.KubernetesContainers.PodAnnotations" : "[]",
        "Octopus.Action.KubernetesContainers.DeploymentAnnotations" : "[]",
        "Octopus.Action.KubernetesContainers.DeploymentLabels" : "{\"app\":\"${local.cartservice_resource_names}\"}",
        "Octopus.Action.KubernetesContainers.CombinedVolumes" : "[]",
        "Octopus.Action.KubernetesContainers.PodSecurityFsGroup" : "1000",
        "Octopus.Action.KubernetesContainers.PodSecurityRunAsGroup" : "1000",
        "Octopus.Action.KubernetesContainers.PodSecurityRunAsNonRoot" : "true",
        "Octopus.Action.KubernetesContainers.PodSecuritySysctls" : "[]",
        "Octopus.Action.KubernetesContainers.PodServiceAccountName" : "default",
        "Octopus.Action.KubernetesContainers.Containers" : local.cartservice_containers,
        "Octopus.Action.KubernetesContainers.PodSecurityRunAsUser" : "1000",
        "Octopus.Action.KubernetesContainers.ServiceName" : local.cartservice_resource_names,
        "Octopus.Action.KubernetesContainers.LoadBalancerAnnotations" : "[]",
        "Octopus.Action.KubernetesContainers.ServicePorts" : local.cartservice_service_ports
        "Octopus.Action.RunOnServer" : "true"
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
        name                      = "cartservice-sbom"
        package_id                = "microservices-demo:cartservice-sbom"
        feed_id                   = octopusdeploy_maven_feed.github_maven_feed.id
        acquisition_location      = "Server"
        extract_during_deployment = true
      }
      script_body = local.vulnerability_scan
    }
  }
}