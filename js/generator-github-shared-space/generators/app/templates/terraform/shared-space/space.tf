data "octopusdeploy_spaces" "app_builder_space" {
  partial_name = substr("${var.octopus_space} ${lower(var.github_repo_owner)}", 0, 18)
  skip         = 0
  take         = 100
}

resource "octopusdeploy_space" "app_builder_space" {
  description                 = "A space created by app builder. This resource is created and managed by the [Octopus Terraform provider](https://registry.terraform.io/providers/OctopusDeployLabs/octopusdeploy/latest/docs). The Terraform files can be found in the [GitHub repo](https://github.com/${var.github_repo})."
  # Attempt to generate a uniquely named space.
  # We can get into a situation where the provider is rerun with a fresh backend, and loses the ID of a previously
  # created space. For example, the GitHub repo may be deleted and recreated, resulting in a fresh backend.
  # In this case we want to create a new, unique space. This is achieved by scanning the Octopus instance
  # for any existing spaces with the same prefix, getting the count, adding 1, and using that as the suffix.
  # This is obviously not completely accurate, as spaces may have been renamed, but should catch most edge cases.
  name                        = "${substr("${var.octopus_space} ${lower(var.github_repo_owner)}", 0, 18)} ${data.octopusdeploy_spaces.app_builder_space.count + 1}"
  is_default                  = false
  is_task_queue_stopped       = false
  space_managers_teams        = ["teams-everyone"]
}

output "octopus_space_id" {
  value = octopusdeploy_space.app_builder_space.id
}

output "octopus_space_name" {
  value = octopusdeploy_space.app_builder_space.name
}