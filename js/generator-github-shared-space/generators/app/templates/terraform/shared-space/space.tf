resource "octopusdeploy_space" "app_builder_space" {
  description                 = "A space created by app builder. This resource is created and managed by the [Octopus Terraform provider](https://registry.terraform.io/providers/OctopusDeployLabs/octopusdeploy/latest/docs). The Terraform files can be found in the [GitHub repo](https://github.com/${var.github_repo})."
  name                        = substr("${var.octopus_space} ${split(var.github_repo, "/")[0]}", 0, 20)
  is_default                  = false
  is_task_queue_stopped       = false
  space_managers_teams        = ["teams-everyone"]
}

output "octopus_space_id" {
  value = octopusdeploy_space.app_builder_space.id
}