locals {
  productcatalogservice_package_name        = "octopussamples/productcatalogservice"
  productcatalogservice_resource_names      = "productcatalogservice"
  productcatalogservice_project_name        = "Google Microservice Product Catalog Service"
  productcatalogservice_project_description = "Deploys the product catalog service."
  productcatalogservice_service_ports       = "[{\"name\":\"grpc\",\"port\":\"3550\",\"targetPort\":\"3550\"}]"
  productcatalogservice_containers          = jsonencode([{
    IsNew:true,
    InitContainer:"False",
    Ports:[{
      value:"3550"
    }],
    EnvironmentVariables:[{
      key:"PORT",
      value:"3550"
    },
      {
        key:"DISABLE_STATS",
        value:"1"
      },
      {
        key:"DISABLE_TRACING",
        value:"1"
      },
      {
        key:"DISABLE_PROFILER",
        value:"1"
      }],
    SecretEnvironmentVariables:[],
    SecretEnvFromSource:[],
    ConfigMapEnvironmentVariables:[],
    ConfigMapEnvFromSource:[],
    FieldRefEnvironmentVariables:[],
    VolumeMounts:[],
    AcquisitionLocation:"NotAcquired",
    Name : local.productcatalogservice_resource_names
    PackageId : local.productcatalogservice_package_name
    FeedId : var.octopus_dockerhub_feed_id
    Properties:{

    },
    Command:[],
    Args:[],
    Resources:{
      requests:{
        memory:"64Mi",
        cpu:"100m",
        ephemeralStorage:""
      },
      limits:{
        memory:"128Mi",
        cpu:"200m",
        ephemeralStorage:"",
        nvidiaGpu:"",
        amdGpu:""
      }
    },
    LivenessProbe:{
      failureThreshold:"",
      initialDelaySeconds:"",
      periodSeconds:"",
      successThreshold:"",
      timeoutSeconds:"",
      type:"Command",
      exec:{
        command:["/bin/grpc_health_probe",
          "-addr=:3550"]
      },
      httpGet:{
        host:"",
        path:"",
        port:"",
        scheme:"",
        httpHeaders:[]
      },
      tcpSocket:{
        host:"",
        port:""
      }
    },
    ReadinessProbe:{
      failureThreshold:"",
      initialDelaySeconds:"",
      periodSeconds:"",
      successThreshold:"",
      timeoutSeconds:"",
      type:"Command",
      exec:{
        command:["/bin/grpc_health_probe",
          "-addr=:3550"]
      },
      httpGet:{
        host:"",
        path:"",
        port:"",
        scheme:"",
        httpHeaders:[]
      },
      tcpSocket:{
        host:"",
        port:""
      }
    },
    StartupProbe:{
      failureThreshold:"",
      initialDelaySeconds:"",
      periodSeconds:"",
      successThreshold:"",
      timeoutSeconds:"",
      type:null,
      exec:{
        command:[]
      },
      httpGet:{
        host:"",
        path:"",
        port:"",
        scheme:"",
        httpHeaders:[]
      },
      tcpSocket:{
        host:"",
        port:""
      }
    },
    Lifecycle:{

    },
    SecurityContext:{
      allowPrivilegeEscalation:"false",
      privileged:"false",
      readOnlyRootFilesystem:"true",
      runAsGroup:"",
      runAsNonRoot:"",
      runAsUser:"",
      capabilities:{
        add:[],
        drop:["all"]
      },
      seLinuxOptions:{
        level:"",
        role:"",
        type:"",
        user:""
      }
    }
  }])
}

resource "octopusdeploy_project" "productcatalogservice_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = local.productcatalogservice_project_description
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_application_lifecycle_id
  name                                 = local.productcatalogservice_project_name
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

resource "octopusdeploy_channel" "productcatalogservice_feature_branch" {
  name        = "Feature Branches"
  project_id  = octopusdeploy_project.productcatalogservice_project.id
  description = "The channel through which feature branches are deployed"
  depends_on  = [octopusdeploy_deployment_process.productcatalogservice_deployment_process]
  is_default  = false
  rule {
    tag = ".+"
    action_package {
      deployment_action = local.deployment_step
      package_reference = local.productcatalogservice_resource_names
    }
  }
}

resource "octopusdeploy_channel" "productcatalogservice_mainline" {
  name        = "Mainline"
  project_id  = octopusdeploy_project.productcatalogservice_project.id
  description = "The channel through which mainline releases are deployed"
  depends_on  = [octopusdeploy_deployment_process.productcatalogservice_deployment_process]
  is_default  = true
  rule {
    tag = "^$"
    action_package {
      deployment_action = local.deployment_step
      package_reference = local.productcatalogservice_resource_names
    }
  }
}

resource "octopusdeploy_variable" "productcatalogservice_debug_variable" {
  name         = "OctopusPrintVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.productcatalogservice_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "productcatalogservice_debug_evaluated_variable" {
  name         = "OctopusPrintEvaluatedVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.productcatalogservice_project.id
  value        = "False"
}

resource "octopusdeploy_deployment_process" "productcatalogservice_deployment_process" {
  project_id = octopusdeploy_project.productcatalogservice_project.id
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
        name                      = local.productcatalogservice_resource_names
        package_id                = local.productcatalogservice_package_name
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
        "Octopus.Action.KubernetesContainers.DeploymentName" : local.productcatalogservice_resource_names,
        "Octopus.Action.KubernetesContainers.DnsConfigOptions" : "[]",
        "Octopus.Action.KubernetesContainers.PodAnnotations" : "[{\"key\":\"sidecar.istio.io/rewriteAppHTTPProbers\",\"value\":\"true\"}]",
        "Octopus.Action.KubernetesContainers.DeploymentAnnotations" : "[]",
        "Octopus.Action.KubernetesContainers.DeploymentLabels" : "{\"app\":\"frontend\"}",
        "Octopus.Action.KubernetesContainers.CombinedVolumes" : "[]",
        "Octopus.Action.KubernetesContainers.PodSecurityFsGroup" : "1000",
        "Octopus.Action.KubernetesContainers.PodSecurityRunAsGroup" : "1000",
        "Octopus.Action.KubernetesContainers.PodSecurityRunAsNonRoot" : "true",
        "Octopus.Action.KubernetesContainers.PodSecuritySysctls" : "[]",
        "Octopus.Action.KubernetesContainers.PodServiceAccountName" : "default",
        "Octopus.Action.KubernetesContainers.Containers" : local.productcatalogservice_containers,
        "Octopus.Action.KubernetesContainers.PodSecurityRunAsUser" : "1000",
        "Octopus.Action.KubernetesContainers.ServiceName" : local.productcatalogservice_resource_names,
        "Octopus.Action.KubernetesContainers.LoadBalancerAnnotations" : "[]",
        "Octopus.Action.KubernetesContainers.ServicePorts" : local.productcatalogservice_service_ports
        "Octopus.Action.RunOnServer" : "true"
      }
    }
  }
}