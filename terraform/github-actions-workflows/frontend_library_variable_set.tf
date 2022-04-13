resource "octopusdeploy_library_variable_set" "frontend_library_variable_set" {
  name = "GitHub Actions Workflow Frontend"
  description = "Variables used when deploying the GitHub Actions Workflow Frontend"
}

output "library_variable_set_id" {
  value = octopusdeploy_library_variable_set.frontend_library_variable_set.id
}



resource "octopusdeploy_variable" "title" {
  name = "S3.Directory"
  type = "String"
  description = "The directory holding the frontend files - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "#{Octopus.Action[Upload Frontend].Package[].PackageId}.#{Octopus.Action[Upload Frontend].Package[].PackageVersion}"
}

resource "octopusdeploy_variable" "title" {
  name = "settings:title"
  type = "String"
  description = "The app title - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "GitHub Actions Workflow Generator"
}

resource "octopusdeploy_variable" "generate_api_path" {
  name = "settings:generateApiPath"
  type = "String"
  description = "The generate API endpoint - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "/api/pipeline/github/generate"
}

resource "octopusdeploy_variable" "github_login_development" {
  name = "settings:github:loginPath"
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
  name = "settings:github:loginPath"
  type = "String"
  description = "The path to redirect to when performing a GitHub Login - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "https://githubactionsworkflowgenerator.octopus.com/oauth/github/login"
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}

resource "octopusdeploy_variable" "hostname_development" {
  name = "WebApp.Hostname"
  type = "String"
  description = "The path to redirect to when performing a GitHub Login - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "development.githubactionworkflows.com"
  scope {
    environments = [var.octopus_development_environment_id, var.octopus_development_security_environment_id]
  }
}

resource "octopusdeploy_variable" "hostname_production" {
  name = "WebApp.Hostname"
  type = "String"
  description = "The path to redirect to when performing a GitHub Login - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "githubactionsworkflowgenerator.octopus.com"
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}