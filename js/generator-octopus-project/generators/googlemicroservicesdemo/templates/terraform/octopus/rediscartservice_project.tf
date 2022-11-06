locals {
  rediscartservice_package_id          = "redis"
  rediscartservice_package_name        = "redis-cart"
  rediscartservice_resource_names      = "redis-cart#{unless Octopus.Release.Channel.Name == \"Mainline\"}-#{Octopus.Release.Channel.Name}#{/unless}"
  rediscartservice_project_name        = "Redis Cart Service"
  rediscartservice_project_description = "Deploys the redis cart service."
  rediscartservice_service_ports       = jsonencode([
    {
      name : "tls-redis",
      port : "6379",
      targetPort : "6379"
    }
  ])
  rediscartservice_containers = jsonencode([
    {
      IsNew : true,
      InitContainer : "False",
      Ports : [
        {
          value : "6379"
        }
      ],
      EnvironmentVariables : [
      ],
      SecretEnvironmentVariables : [
      ],
      SecretEnvFromSource : [
      ],
      ConfigMapEnvironmentVariables : [
      ],
      ConfigMapEnvFromSource : [
      ],
      FieldRefEnvironmentVariables : [
      ],
      VolumeMounts : [
        {
          key : "redis-data",
          value : "/data",
          option : ""
        }
      ],
      AcquisitionLocation : "NotAcquired",
      Name : "redis",
      PackageId : "redis",
      FeedId : "Feeds-1193",
      Properties : {
      },
      Command : [
      ],
      Args : [
      ],
      Resources : {
        requests : {
          memory : "200Mi",
          cpu : "70m",
          ephemeralStorage : ""
        },
        limits : {
          memory : "256Mi",
          cpu : "125m",
          ephemeralStorage : "",
          nvidiaGpu : "",
          amdGpu : ""
        }
      },
      LivenessProbe : {
        failureThreshold : "",
        initialDelaySeconds : "",
        periodSeconds : "5",
        successThreshold : "",
        timeoutSeconds : "",
        type : "TcpSocket",
        exec : {
          command : [
          ]
        },
        httpGet : {
          host : "",
          path : "",
          port : "",
          scheme : "",
          httpHeaders : [
          ]
        },
        tcpSocket : {
          host : "",
          port : "6379"
        }
      },
      ReadinessProbe : {
        failureThreshold : "",
        initialDelaySeconds : "",
        periodSeconds : "5",
        successThreshold : "",
        timeoutSeconds : "",
        type : "TcpSocket",
        exec : {
          command : [
          ]
        },
        httpGet : {
          host : "",
          path : "",
          port : "",
          scheme : "",
          httpHeaders : [
          ]
        },
        tcpSocket : {
          host : "",
          port : "6379"
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
          command : [
          ]
        },
        httpGet : {
          host : "",
          path : "",
          port : "",
          scheme : "",
          httpHeaders : [
          ]
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
          add : [
          ],
          drop : [
            "all"
          ]
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

resource "octopusdeploy_project" "rediscartservice_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = local.rediscartservice_project_description
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_simple_lifecycle_id
  name                                 = local.rediscartservice_project_name
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

resource "octopusdeploy_channel" "rediscartservice_mainline" {
  name        = "Mainline"
  project_id  = octopusdeploy_project.rediscartservice_project.id
  description = "The channel through which mainline releases are deployed"
  depends_on  = [octopusdeploy_deployment_process.rediscartservice_deployment_process]
  is_default  = true
  rule {
    tag = "^$"
    action_package {
      deployment_action = local.deployment_step
      package_reference = local.rediscartservice_package_name
    }
  }
}

resource "octopusdeploy_variable" "rediscartservice_debug_variable" {
  name         = "OctopusPrintVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.rediscartservice_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "rediscartservice_debug_evaluated_variable" {
  name         = "OctopusPrintEvaluatedVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.rediscartservice_project.id
  value        = "False"
}

resource "octopusdeploy_deployment_process" "rediscartservice_deployment_process" {
  project_id = octopusdeploy_project.rediscartservice_project.id
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
      features     = ["Octopus.Features.KubernetesService"]
      package {
        name                      = local.rediscartservice_package_name
        package_id                = local.rediscartservice_package_id
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
        "Octopus.Action.KubernetesContainers.DeploymentName" : local.rediscartservice_resource_names,
        "Octopus.Action.KubernetesContainers.DnsConfigOptions" : "[]",
        "Octopus.Action.KubernetesContainers.PodAnnotations" : "[]",
        "Octopus.Action.KubernetesContainers.DeploymentAnnotations" : "[]",
        "Octopus.Action.KubernetesContainers.DeploymentLabels" : "{\"app\":\"${local.rediscartservice_package_name}\"}",
        "Octopus.Action.KubernetesContainers.CombinedVolumes" : "[{\"Name\":\"redis-data\",\"Type\":\"EmptyDir\",\"EmptyDirMedium\":\"\"}]",
        "Octopus.Action.KubernetesContainers.PodSecurityFsGroup" : "1000",
        "Octopus.Action.KubernetesContainers.PodSecurityRunAsGroup" : "1000",
        "Octopus.Action.KubernetesContainers.PodSecurityRunAsNonRoot" : "true",
        "Octopus.Action.KubernetesContainers.PodSecuritySysctls" : "[]",
        "Octopus.Action.KubernetesContainers.Containers" : local.rediscartservice_containers,
        "Octopus.Action.KubernetesContainers.PodSecurityRunAsUser" : "1000",
        "Octopus.Action.KubernetesContainers.ServiceName" : local.rediscartservice_resource_names,
        "Octopus.Action.KubernetesContainers.LoadBalancerAnnotations" : "[]",
        "Octopus.Action.KubernetesContainers.ServicePorts" : local.rediscartservice_service_ports
        "Octopus.Action.RunOnServer" : "true"
      }
    }
  }
}