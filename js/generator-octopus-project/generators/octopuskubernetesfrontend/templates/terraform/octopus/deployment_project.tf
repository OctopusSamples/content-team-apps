resource "octopusdeploy_project" "deploy_frontend_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = var.octopus_project_description
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_lifecycle_id
  name                                 = var.octopus_project_name
  project_group_id                     = var.existing_project_group ? data.octopusdeploy_project_groups.project_group.project_groups[0].id : octopusdeploy_project_group.project_group[0].id
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

resource "octopusdeploy_variable" "dockerhub_username" {
  name         = "DockerHub.Username"
  type         = "String"
  description  = "The DockerHub username"
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_frontend_project.id
  value        = var.dockerhub_username
}

resource "octopusdeploy_variable" "dockerhub_password" {
  name         = "DockerHub.Password"
  type         = "Sensitive"
  description  = "The DockerHub password."
  is_sensitive = true
  owner_id     = octopusdeploy_project.deploy_frontend_project.id
  value        = var.dockerhub_password
}

locals {
  frontend_package_name = "frontend"
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
        var.octopus_development_environment_id,
        var.octopus_production_environment_id
      ]
      features = ["Octopus.Features.KubernetesService", "Octopus.Features.KubernetesIngress"]
      package {
        name                      = local.frontend_package_name
        package_id                = var.docker_image
        feed_id                   = var.octopus_dockerhub_feed_id
        acquisition_location      = "NotAcquired"
        extract_during_deployment = false
      }
      container {
        feed_id = var.octopus_dockerhub_feed_id
        image   = "octopusdeploy/worker-tools:3-ubuntu.18.04"
      }
      properties = {
        "Octopus.Action.KubernetesContainers.CombinedVolumes" : "[]",
        "Octopus.Action.KubernetesContainers.Containers" : jsonencode([
          {
            Name : local.frontend_package_name
            Ports : [
              {
                key : "web"
                value : 80
              }
            ],
            Resources : {
              requests : {
                memory : "256Mi"
                cpu : ""
              }
              limits : {
                memory : "1Gi",
                cpu : ""
              }
            },
            InitContainer : false,
          }
        ])
        "Octopus.Action.KubernetesContainers.DeploymentAnnotations" : "[]",
        "Octopus.Action.KubernetesContainers.DeploymentLabels" : jsonencode({ app : "frontend" }),
        "Octopus.Action.KubernetesContainers.DeploymentName" : "frontend",
        "Octopus.Action.KubernetesContainers.DeploymentResourceType" : "Deployment",
        "Octopus.Action.KubernetesContainers.DeploymentStyle" : "RollingUpdate",
        "Octopus.Action.KubernetesContainers.DeploymentWait" : "Wait",
        "Octopus.Action.KubernetesContainers.DnsConfigOptions" : "[]",
        "Octopus.Action.KubernetesContainers.IngressAnnotations" : jsonencode([
          {
            key : "alb.ingress.kubernetes.io/group.order"
            value : "500"
          },
          {
            key : "alb.ingress.kubernetes.io/scheme"
            value : "internet-facing"
          },
          {
            key : "alb.ingress.kubernetes.io/healthcheck-path"
            value : "/"
          },
          {
            key : "alb.ingress.kubernetes.io/target-type"
            value : "ip"
          },
          {
            key : "kubernetes.io/ingress.class",
            value : "alb"
          }
        ]),
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
        "Octopus.Action.KubernetesContainers.IngressRules" : jsonencode([
          {
            host : ""
            http : {
              paths : [
                {
                  key : "/*"
                  value : "web",
                  option2 : "ImplementationSpecific"
                }
              ]
            }
          }
        ])
        "Octopus.Action.KubernetesContainers.ServiceName" : local.frontend_service_name,
        "Octopus.Action.KubernetesContainers.ServicePorts" : jsonencode([
          {
            name : "web"
            port : "80"
            targetPort : var.docker_image_port
            nodePort : ""
            protocol : "TCP"
          }
        ]),
        "Octopus.Action.KubernetesContainers.ConfigMapName" : "",
        "Octopus.Action.KubernetesContainers.ConfigMapValues" : ""
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
        var.octopus_development_environment_id,
        var.octopus_production_environment_id
      ]
      container {
        feed_id = var.octopus_dockerhub_feed_id
        image   = "octopusdeploy/worker-tools:3-ubuntu.18.04"
      }
      properties = {
        "Octopus.Action.Script.ScriptSource" : "Inline"
        "Octopus.Action.Script.Syntax" : "Bash"
        "Octopus.Action.Script.ScriptBody" : templatefile("../../bash/${var.project_name}/display-ingress.sh", {
          frontend_ingress_name = local.frontend_ingress_name
        })
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
        var.octopus_development_environment_id,
        var.octopus_production_environment_id
      ]
      script_body = file("../../bash/${var.project_name}/smoke-test.sh")
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
        var.octopus_development_security_environment_id,
        var.octopus_production_security_environment_id
      ]
      script_body = templatefile("../../bash/${var.project_name}/docker-scan.sh", {
        image : var.docker_image
        frontend : local.frontend_package_name
      })
    }
  }
}