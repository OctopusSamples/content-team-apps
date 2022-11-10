resource "octopusdeploy_space" "app_builder_space" {
  description                 = "A space created by Octopus Builder. This resource is created and managed by the [Octopus Terraform provider](https://registry.terraform.io/providers/OctopusDeployLabs/octopusdeploy/latest/docs). The Terraform files can be found in the [GitHub repo](https://github.com/${var.github_repo})."
  name                        = var.octopus_space
  is_default                  = false
  is_task_queue_stopped       = false
  space_managers_teams        = ["teams-managers"]
  space_managers_team_members = []
}

output "octopus_space_id" {
  value = octopusdeploy_space.app_builder_space.id
}

output "octopus_space_name" {
  value = octopusdeploy_space.app_builder_space.name
}