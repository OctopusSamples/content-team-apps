resource "octopusdeploy_channel" "feature_branch" {
  name        = "Feature Branches"
  project_id  = octopusdeploy_project.deploy_project.id
  description = "The channel through which feature branches are deployed"
  depends_on = [octopusdeploy_project.deploy_project]
  rule {
    tag = ".+"
    action_package {
      deployment_action = "Upload Lambda"
    }
  }
}

resource "octopusdeploy_channel" "mainline" {
  name        = "Mainline"
  project_id  = octopusdeploy_project.deploy_project.id
  description = "The channel through which mainline releases are deployed"
  is_default  = true
  depends_on = [octopusdeploy_project.deploy_project]
  rule {
    tag = "^$"
    action_package {
      deployment_action = "Upload Lambda"
    }
  }
}