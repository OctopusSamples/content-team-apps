resource "octopusdeploy_library_variable_set" "frontend_library_variable_set" {
  name = "GitHub Actions Workflow Frontend"
  description = "Variables used when deploying the GitHub Actions Workflow Frontend"
}

output "library_variable_set_id" {
  value = octopusdeploy_library_variable_set.frontend_library_variable_set.id
}

resource "octopusdeploy_variable" "s3_directory" {
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

resource "octopusdeploy_variable" "google_tag" {
  name = "settings:google:tag"
  type = "String"
  description = "The Google analytics tag - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "GTM-M6BF84M"
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

resource "octopusdeploy_variable" "cognito_development" {
  name = "settings:aws:cognitoLogin"
  type = "String"
  description = "The cognito login page - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "https://content-team-octopus.auth.us-west-1.amazoncognito.com/login?client_id=4r0atff7ovqbhrpe1773k37vk9&response_type=token&scope=email+openid+profile&redirect_uri=https://development.githubactionworkflows.com/"
  scope {
    environments = [var.octopus_development_environment_id, var.octopus_development_security_environment_id]
  }
}

resource "octopusdeploy_variable" "cognito_production" {
  name = "settings:aws:cognitoLogin"
  type = "String"
  description = "The cognito login page - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "https://content-team-octopus-production.auth.us-west-1.amazoncognito.com/login?client_id=44099ejdg4jgiko2cpfv9pun11&response_type=token&scope=email+openid+profile&redirect_uri=https://githubactionsworkflowgenerator.octopus.com"
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}

resource "octopusdeploy_variable" "audit_development" {
  name = "settings:auditEndpoint"
  type = "String"
  description = "The audit endpoint - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "https://b3u42cdd6h.execute-api.us-west-1.amazonaws.com/Development/api/audits"
  scope {
    environments = [var.octopus_development_environment_id, var.octopus_development_security_environment_id]
  }
}

resource "octopusdeploy_variable" "audit_production" {
  name = "settings:auditEndpoint"
  type = "String"
  description = "The audit endpoint - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "https://dodwbeqe6g.execute-api.us-west-1.amazonaws.com/Production/api/audits"
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}

resource "octopusdeploy_variable" "health_development" {
  name = "settings:healthEndpoint"
  type = "String"
  description = "The audit health endpoint - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "https://b3u42cdd6h.execute-api.us-west-1.amazonaws.com/Development/health"
  scope {
    environments = [var.octopus_development_environment_id, var.octopus_development_security_environment_id]
  }
}

resource "octopusdeploy_variable" "health_production" {
  name = "settings:healthEndpoint"
  type = "String"
  description = "The audit health endpoint - update the Terraform files in [GitHub](https://github.com/OctopusSamples/content-team-apps/terraform) instead."
  is_sensitive = false
  owner_id = octopusdeploy_library_variable_set.frontend_library_variable_set.id
  value = "https://dodwbeqe6g.execute-api.us-west-1.amazonaws.com/Production/health"
  scope {
    environments = [var.octopus_production_environment_id, var.octopus_production_security_environment_id]
  }
}