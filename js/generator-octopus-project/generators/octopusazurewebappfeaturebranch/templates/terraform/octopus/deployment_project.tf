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
    allow_deployments_to_no_targets = true
    exclude_unhealthy_targets       = false
    skip_machine_behavior           = "SkipUnavailableMachines"
  }
}

resource "octopusdeploy_variable" "azure_account_development" {
  name         = "Octopus.Azure.Account"
  type         = "AzureAccount"
  description  = "The development azure account. This is used for target discovery, and for general scripting."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_frontend_project.id
  value        = var.octopus_azure_development_account_id
}

resource "octopusdeploy_variable" "azure_account_production" {
  name         = "Unused"
  type         = "String"
  description  = "Unused"
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_frontend_project.id
  value        = "unused"
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

resource "octopusdeploy_variable" "frontend_featurebranch_variable" {
  name         = "FeatureBranch"
  type         = "String"
  description  = "The name of the feature branch."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_frontend_project.id
  value        = "#{Octopus.Action[Deploy WebApp].Package[].PackageVersion | VersionPreRelease | Replace \"\\..*\" \"\"}"
}

resource "octopusdeploy_variable" "frontend_fixedfeaturebranch_variable" {
  name         = "FixedFeatureBranch"
  type         = "String"
  description  = "The name of the feature branch."
  is_sensitive = false
  owner_id     = octopusdeploy_project.deploy_frontend_project.id
  value        = "#{if FeatureBranch}#{FeatureBranch}#{/if}#{unless FeatureBranch}master#{/unless}"
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
  name            = "DockerHub.Password"
  type            = "Sensitive"
  description     = "The DockerHub password."
  is_sensitive    = true
  owner_id        = octopusdeploy_project.deploy_frontend_project.id
  sensitive_value = var.dockerhub_password
}

resource "octopusdeploy_deployment_process" "deploy_frontend" {
  project_id = octopusdeploy_project.deploy_frontend_project.id

  step {
    condition           = "Success"
    name                = "Create WebApp Instance"
    package_requirement = "LetOctopusDecide"
    start_trigger       = "StartAfterPrevious"
    action {
      action_type    = "Octopus.AzurePowerShell"
      name           = "Create WebApp Instance"
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      container {
        feed_id = var.octopus_dockerhub_feed_id
        image   = "octopusdeploy/worker-tools:3-ubuntu.18.04"
      }
      properties = {
        "Octopus.Action.Script.ScriptSource" : "Inline"
        "Octopus.Action.Script.Syntax" : "Bash"
        "Octopus.Action.Azure.AccountId" : "#{Octopus.Azure.Account}"
        "Octopus.Action.Script.ScriptBody" : templatefile("../../bash/${var.project_name}/create-webapp.sh", {

        })
        "OctopusUseBundledTooling" : "False"
      }
    }
  }

  step {
    name                 = "Deploy WebApp"
    package_requirement  = "LetOctopusDecide"
    start_trigger        = "StartAfterPrevious"
    target_roles         = ["WebApp"]
    condition            = "Success"
    action {
      action_type    = "Octopus.AzureAppService"
      name           = "Deploy WebApp"
      run_on_server  = true
      worker_pool_id = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      container {
        feed_id = var.octopus_dockerhub_feed_id
        image   = "octopusdeploy/worker-tools:3-ubuntu.18.04"
      }
      primary_package {
        acquisition_location = "NotAcquired"
        feed_id              = var.octopus_dockerhub_feed_id
        package_id           = var.docker_image
        properties           = {
          "SelectionMode" : "immediate"
        }
      }
      properties = {
        "OctopusUseBundledTooling" : "False"
        "Octopus.Action.Azure.DeploymentType" : "Container"
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
      worker_pool_id                     = data.octopusdeploy_worker_pools.ubuntu_worker_pool.worker_pools[0].id
      name                               = "Check for Vulnerabilities"
      script_body                        = templatefile("../../bash/${var.project_name}/docker-scan.sh", {
        docker_image : var.docker_image
      })
    }
  }
}