resource "octopusdeploy_project" "completedeployment_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploy the complete microservice stack"
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = var.octopus_application_lifecycle_id
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
    name                = "Ad Service"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    target_roles        = [local.deployment_role]
    action {
      action_type    = "Octopus.DeployRelease"
      name           = "Ad Service"
      run_on_server  = true
      environments   = [
        var.octopus_development_app_environment_id,
        var.octopus_production_app_environment_id
      ]
      features = []
      package {
        name                      = ""
        package_id                = octopusdeploy_project.adservice_project.id
        feed_id                   = var.octopus_built_in_feed_id
        acquisition_location      = "NotAcquired"
        extract_during_deployment = false
      }
      properties = {
        "Octopus.Action.DeployRelease.DeploymentCondition": "Always",
        "Octopus.Action.DeployRelease.ProjectId": octopusdeploy_project.adservice_project.id
      }
    }
  }
}