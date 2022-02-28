resource "octopusdeploy_project" "deploy_backend_project" {
  auto_create_release                  = false
  default_guided_failure_mode          = "EnvironmentDefault"
  default_to_skip_if_already_installed = false
  description                          = "Deploys the backend service."
  discrete_channel_release             = false
  is_disabled                          = false
  is_discrete_channel_release          = false
  is_version_controlled                = false
  lifecycle_id                         = data.octopusdeploy_lifecycles.application_lifecycle.id
  name                                 = "Backend"
  project_group_id                     = octopusdeploy_project_group.backend_project_group.id
  tenanted_deployment_participation    = "Untenanted"
  space_id                             = var.octopus_space_id

  connectivity_policy {
    allow_deployments_to_no_targets = false
    exclude_unhealthy_targets       = false
    skip_machine_behavior           = "SkipUnavailableMachines"
  }
}

resource "octopusdeploy_deployment_process" "deploy_backend_step1" {
  project_id = octopusdeploy_project.deploy_backend_project.id
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
        EOT
      run_on_server                      = true
      worker_pool_id                     = data.octopusdeploy_worker_pools.ubuntu_worker_pool.id
    }
  }
}