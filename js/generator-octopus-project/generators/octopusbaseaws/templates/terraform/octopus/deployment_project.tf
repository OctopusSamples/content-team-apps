resource "octopusdeploy_project" "deploy_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = var.octopus_project_description
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  name                                 = var.octopus_project_name
  lifecycle_id                         = var.octopus_lifecycle_id
  project_group_id                     = var.existing_project_group ? data.octopusdeploy_project_groups.project_group.project_groups[0].id : octopusdeploy_project_group.project_group.id
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

output "deploy_project_id" {
  value = octopusdeploy_project.deploy_project.id
}

resource "octopusdeploy_variable" "debug_variable" {
  name         = "OctopusPrintVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_project.id
  value        = "False"
}

resource "octopusdeploy_variable" "debug_evaluated_variable" {
  name         = "OctopusPrintEvaluatedVariables"
  type         = "String"
  description  = "A debug variable used to print all variables to the logs. See [here](https://octopus.com/docs/support/debug-problems-with-octopus-variables) for more information."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_project.id
  value        = "False"
}

resource "octopusdeploy_deployment_process" "deployment_process" {
  project_id = octopusdeploy_project.deploy_project.id
  step {
    condition           = "Success"
    name                = "Hello world (using Bash)"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartWithPrevious"
    run_script_action {
      can_be_used_for_project_versioning = false
      condition                          = "Success"
      is_disabled                        = false
      is_required                        = true
      name                               = "Hello world (using Bash)"
      script_body                        = <<-EOT
          echo 'Hello world, using Bash'
          #TODO: Experiment with steps of your own :)
          echo '[Learn more about the types of steps available in Octopus](https://g.octopushq.com/OnboardingAddStepsLearnMore)'
        EOT
      run_on_server                      = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
    }
  }
}