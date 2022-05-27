resource "octopusdeploy_library_variable_set" "octopus_library_variable_set" {
  name = "Octopus Variables"
  description = "Variables that relate to working with Octopus itself, such as API keys. This resource is created and managed by the [Octopus Terraform provider](https://registry.terraform.io/providers/OctopusDeployLabs/octopusdeploy/latest/docs). The Terraform files can be found in the [GitHub repo](https://github.com/${var.github_repo})."
}

output "octopus_library_variable_set_id" {
  value = octopusdeploy_library_variable_set.octopus_library_variable_set.id
}


resource "octopusdeploy_variable" "octopus_admin_api_key" {
  name = "Octopus.AdminApiKey"
  type = "Sensitive"
  description = "The API key created by the Octopus Builder. This key is used to manage the Octopus instance."
  is_sensitive = true
  owner_id = octopusdeploy_library_variable_set.octopus_library_variable_set.id
  value = var.octopus_apikey
}