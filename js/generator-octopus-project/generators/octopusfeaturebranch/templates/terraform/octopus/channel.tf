resource "octopusdeploy_channel" "channel" {
  name       = "Feature branch ${var.featurebranch_name}"
  project_id = data.octopusdeploy_projects.project.projects[0].id
  lifecycle_id = "${octopusdeploy_lifecycle.lifecycle.id}"
  description = "Deployment for ${var.featurebranch_name}"
  rule {
    id = var.featurebranch_name
    tag = "^${var.featurebranch_name}.*$"
    action_package {
      deployment_action = var.step_name
    }
  }
}