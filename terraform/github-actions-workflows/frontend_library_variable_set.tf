resource "octopusdeploy_library_variable_set" "frontend_library_variable_set" {
  name = "GitHub Actions Workflow Frontend"
  description = "Variables used when deploying the GitHub Actions Workflow Frontend"
}

output "library_variable_set_id" {
  value = octopusdeploy_library_variable_set.frontend_library_variable_set.id
}

resource "octopusdeploy_variable" "github_login_development" {
  name = "github:loginPath"
  type = "String"
  description = "The path to redirect to when performing a GitHub Login - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "https://development.githubactionworkflows.com/oauth/github/login"
  scope {
    environments = [var.octopus_development_environment_id, var.octopus_development_security_environment_id]
  }
}

resource "octopusdeploy_variable" "github_login_production" {
  name = "github:loginPath"
  type = "String"
  description = "The path to redirect to when performing a GitHub Login - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "https://githubactionsworkflowgenerator.octopus.com/oauth/github/login"
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}