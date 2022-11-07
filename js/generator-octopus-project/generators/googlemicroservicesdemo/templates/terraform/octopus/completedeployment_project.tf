resource "octopusdeploy_project" "completedeployment_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploy the complete microservice stack"
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_production_only_lifecycle_id
  name                                 = "Complete Deployment"
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

resource "octopusdeploy_variable" "completedeployment_debug_variable" {
  name         = "OctopusPrintVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.completedeployment_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "completedeployment_debug_evaluated_variable" {
  name         = "OctopusPrintEvaluatedVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.completedeployment_project.id
  value        = "False"
}

resource "octopusdeploy_deployment_process" "completedeployment_deployment_process" {
  project_id = octopusdeploy_project.completedeployment_project.id
  step {
    condition           = "Success"
    name                = "Redis Cart Service"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = [local.deployment_role]
    action {
      action_type    = "Octopus.DeployRelease"
      name           = "Redis Cart Service"
      run_on_server  = true
      environments   = [
        var.octopus_development_app_environment_id,
        var.octopus_production_app_environment_id
      ]
      features = []
      primary_package {
        package_id                = octopusdeploy_project.rediscartservice_project.id
        feed_id                   = data.octopusdeploy_feeds.built-in-feed.feeds[0].id
        acquisition_location      = "NotAcquired"
      }
      properties = {
        "Octopus.Action.DeployRelease.DeploymentCondition": "Always",
        "Octopus.Action.DeployRelease.ProjectId": octopusdeploy_project.rediscartservice_project.id
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Ad Service"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = [local.deployment_role]
    action {
      action_type    = "Octopus.DeployRelease"
      name           = "Ad Service"
      run_on_server  = true
      excluded_environments   = [
        var.octopus_development_security_environment_id,
        var.octopus_production_security_environment_id
      ]
      features = []
      package {
        name                      = ""
        package_id                = octopusdeploy_project.adservice_project.id
        feed_id                   = data.octopusdeploy_feeds.built-in-feed.feeds[0].id
        acquisition_location      = "NotAcquired"
        extract_during_deployment = false
      }
      properties = {
        "Octopus.Action.DeployRelease.DeploymentCondition": "Always",
        "Octopus.Action.DeployRelease.ProjectId": octopusdeploy_project.adservice_project.id
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Cart Service"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = [local.deployment_role]
    action {
      action_type    = "Octopus.DeployRelease"
      name           = "Cart Service"
      run_on_server  = true
      environments   = [
        var.octopus_development_app_environment_id,
        var.octopus_production_app_environment_id
      ]
      features = []
      primary_package {
        package_id                = octopusdeploy_project.cartservice_project.id
        feed_id                   = data.octopusdeploy_feeds.built-in-feed.feeds[0].id
        acquisition_location      = "NotAcquired"
      }
      properties = {
        "Octopus.Action.DeployRelease.DeploymentCondition": "Always",
        "Octopus.Action.DeployRelease.ProjectId": octopusdeploy_project.cartservice_project.id
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Checkout Service"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = [local.deployment_role]
    action {
      action_type    = "Octopus.DeployRelease"
      name           = "Checkout Service"
      run_on_server  = true
      environments   = [
        var.octopus_development_app_environment_id,
        var.octopus_production_app_environment_id
      ]
      features = []
      primary_package {
        package_id                = octopusdeploy_project.checkoutservice_project.id
        feed_id                   = data.octopusdeploy_feeds.built-in-feed.feeds[0].id
        acquisition_location      = "NotAcquired"
      }
      properties = {
        "Octopus.Action.DeployRelease.DeploymentCondition": "Always",
        "Octopus.Action.DeployRelease.ProjectId": octopusdeploy_project.checkoutservice_project.id
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Currency Service"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = [local.deployment_role]
    action {
      action_type    = "Octopus.DeployRelease"
      name           = "Currency Service"
      run_on_server  = true
      environments   = [
        var.octopus_development_app_environment_id,
        var.octopus_production_app_environment_id
      ]
      features = []
      primary_package {
        package_id                = octopusdeploy_project.currencyservice_project.id
        feed_id                   = data.octopusdeploy_feeds.built-in-feed.feeds[0].id
        acquisition_location      = "NotAcquired"
      }
      properties = {
        "Octopus.Action.DeployRelease.DeploymentCondition": "Always",
        "Octopus.Action.DeployRelease.ProjectId": octopusdeploy_project.currencyservice_project.id
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Email Service"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = [local.deployment_role]
    action {
      action_type    = "Octopus.DeployRelease"
      name           = "Email Service"
      run_on_server  = true
      environments   = [
        var.octopus_development_app_environment_id,
        var.octopus_production_app_environment_id
      ]
      features = []
      primary_package {
        package_id                = octopusdeploy_project.emailservice_project.id
        feed_id                   = data.octopusdeploy_feeds.built-in-feed.feeds[0].id
        acquisition_location      = "NotAcquired"
      }
      properties = {
        "Octopus.Action.DeployRelease.DeploymentCondition": "Always",
        "Octopus.Action.DeployRelease.ProjectId": octopusdeploy_project.emailservice_project.id
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Payment Service"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = [local.deployment_role]
    action {
      action_type    = "Octopus.DeployRelease"
      name           = "Payment Service"
      run_on_server  = true
      environments   = [
        var.octopus_development_app_environment_id,
        var.octopus_production_app_environment_id
      ]
      features = []
      primary_package {
        package_id                = octopusdeploy_project.paymentservice_project.id
        feed_id                   = data.octopusdeploy_feeds.built-in-feed.feeds[0].id
        acquisition_location      = "NotAcquired"
      }
      properties = {
        "Octopus.Action.DeployRelease.DeploymentCondition": "Always",
        "Octopus.Action.DeployRelease.ProjectId": octopusdeploy_project.paymentservice_project.id
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Product Catalog Service"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = [local.deployment_role]
    action {
      action_type    = "Octopus.DeployRelease"
      name           = "Product Catalog Service"
      run_on_server  = true
      environments   = [
        var.octopus_development_app_environment_id,
        var.octopus_production_app_environment_id
      ]
      features = []
      primary_package {
        package_id                = octopusdeploy_project.productcatalogservice_project.id
        feed_id                   = data.octopusdeploy_feeds.built-in-feed.feeds[0].id
        acquisition_location      = "NotAcquired"
      }
      properties = {
        "Octopus.Action.DeployRelease.DeploymentCondition": "Always",
        "Octopus.Action.DeployRelease.ProjectId": octopusdeploy_project.productcatalogservice_project.id
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Recommendation Service"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = [local.deployment_role]
    action {
      action_type    = "Octopus.DeployRelease"
      name           = "Recommendation Service"
      run_on_server  = true
      environments   = [
        var.octopus_development_app_environment_id,
        var.octopus_production_app_environment_id
      ]
      features = []
      primary_package {
        package_id                = octopusdeploy_project.recommendationservice_project.id
        feed_id                   = data.octopusdeploy_feeds.built-in-feed.feeds[0].id
        acquisition_location      = "NotAcquired"
      }
      properties = {
        "Octopus.Action.DeployRelease.DeploymentCondition": "Always",
        "Octopus.Action.DeployRelease.ProjectId": octopusdeploy_project.recommendationservice_project.id
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Shipping Service"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = [local.deployment_role]
    action {
      action_type    = "Octopus.DeployRelease"
      name           = "Shipping Service"
      run_on_server  = true
      environments   = [
        var.octopus_development_app_environment_id,
        var.octopus_production_app_environment_id
      ]
      features = []
      primary_package {
        package_id                = octopusdeploy_project.shippingservice_project.id
        feed_id                   = data.octopusdeploy_feeds.built-in-feed.feeds[0].id
        acquisition_location      = "NotAcquired"
      }
      properties = {
        "Octopus.Action.DeployRelease.DeploymentCondition": "Always",
        "Octopus.Action.DeployRelease.ProjectId": octopusdeploy_project.shippingservice_project.id
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Frontend"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = [local.deployment_role]
    action {
      action_type    = "Octopus.DeployRelease"
      name           = "Frontend"
      run_on_server  = true
      environments   = [
        var.octopus_development_app_environment_id,
        var.octopus_production_app_environment_id
      ]
      features = []
      primary_package {
        package_id                = octopusdeploy_project.frontend_project.id
        feed_id                   = data.octopusdeploy_feeds.built-in-feed.feeds[0].id
        acquisition_location      = "NotAcquired"
      }
      properties = {
        "Octopus.Action.DeployRelease.DeploymentCondition": "Always",
        "Octopus.Action.DeployRelease.ProjectId": octopusdeploy_project.frontend_project.id
      }
    }
  }
  step {
    condition           = "Success"
    name                = "Load Generator"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = [local.deployment_role]
    action {
      action_type    = "Octopus.DeployRelease"
      name           = "Load Generator"
      run_on_server  = true
      environments   = [
        var.octopus_development_app_environment_id,
        var.octopus_production_app_environment_id
      ]
      features = []
      primary_package {
        package_id                = octopusdeploy_project.loadgenerator_project.id
        feed_id                   = data.octopusdeploy_feeds.built-in-feed.feeds[0].id
        acquisition_location      = "NotAcquired"
      }
      properties = {
        "Octopus.Action.DeployRelease.DeploymentCondition": "Always",
        "Octopus.Action.DeployRelease.ProjectId": octopusdeploy_project.loadgenerator_project.id
      }
    }
  }
}