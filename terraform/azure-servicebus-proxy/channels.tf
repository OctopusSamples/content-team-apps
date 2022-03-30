resource "octopusdeploy_channel" "feature_branch" {
  name       = "Feature Branches"
  project_id = octopusdeploy_project.deploy_project.id
  rule {
    tag = ".+"
    action_package {
      deployment_action = "Upload Lambda"
    }
  }
}