resource "octopusdeploy_channel" "feature_branch" {
  name        = "Feature Branches"
  project_id  = octopusdeploy_project.deploy_project.id
  description = "The channel through which feature branches are deployed"
  depends_on = [octopusdeploy_deployment_process.deploy_project]
  rule {
    tag = ".+"
    action_package {
      deployment_action = "Upload Frontend"
    }
  }
}

resource "octopusdeploy_channel" "mainline" {
  name        = "Mainline"
  project_id  = octopusdeploy_project.deploy_project.id
  description = "The channel through which mainline releases are deployed"
  depends_on = [octopusdeploy_deployment_process.deploy_project]
  is_default  = true
  rule {
    tag = "^$"
    action_package {
      deployment_action = "Upload Frontend"
    }
  }
}

resource "octopusdeploy_channel" "security" {
  name        = "Security Testing"
  project_id  = octopusdeploy_project.deploy_project.id
  description = "The channel used to test security"
  depends_on = [octopusdeploy_deployment_process.deploy_project]
  lifecycle_id = var.octopus_security_lifecycle_id
  is_default  = false
  rule {
    tag = "^$"
    action_package {
      deployment_action = "Upload Frontend"
    }
  }
}