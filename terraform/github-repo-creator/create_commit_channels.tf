resource "octopusdeploy_channel" "feature_branch" {
  name        = "Feature Branches"
  project_id  = octopusdeploy_project.create_commit_project.id
  description = "The channel through which feature branches are deployed"
  rule {
    tag = ".+"
    action_package {
      deployment_action = "Upload Lambda"
    }
  }
}

resource "octopusdeploy_channel" "mainline" {
  name        = "Mainline"
  project_id  = octopusdeploy_project.create_commit_project.id
  description = "The channel through which mainline releases are deployed"
  is_default  = true
  rule {
    tag = "^$"
    action_package {
      deployment_action = "Upload Lambda"
    }
  }
}