resource "github_actions_secret" "development_environment" {
  repository       = var.github_repo
  secret_name      = "OCTOPUS_DEVELOPMENT_ENVIRONMENT_ID"
  plaintext_value  = octopusdeploy_environment.development_environment.id
}

resource "github_actions_secret" "development_security_environment" {
  repository       = var.github_repo
  secret_name      = "OCTOPUS_DEVELOPMENT_SECURITY_ENVIRONMENT_ID"
  plaintext_value  = octopusdeploy_environment.development_security_environment.id
}

resource "github_actions_secret" "production_environment" {
  repository       = var.github_repo
  secret_name      = "OCTOPUS_PRODUCTION_ENVIRONMENT_ID"
  plaintext_value  = octopusdeploy_environment.production_environment.id
}

resource "github_actions_secret" "production_security_environment" {
  repository       = var.github_repo
  secret_name      = "OCTOPUS_PRODUCTION_SECURITY_ENVIRONMENT_ID"
  plaintext_value  = octopusdeploy_environment.development_security_environment.id
}

resource "github_actions_secret" "simple_lifecycle_id" {
  repository       = var.github_repo
  secret_name      = "OCTOPUS_SIMPLE_LIFECYCLE_ID"
  plaintext_value  = octopusdeploy_lifecycle.simple_lifecycle.id
}

resource "github_actions_secret" "application_lifecycle_id" {
  repository       = var.github_repo
  secret_name      = "OCTOPUS_APPLICATION_LIFECYCLE_ID"
  plaintext_value  = octopusdeploy_lifecycle.application_lifecycle.id
}

resource "github_actions_secret" "productiononly_lifecycle_id" {
  repository       = var.github_repo
  secret_name      = "OCTOPUS_PRODUCTION_ONLY_LIFECYCLE_ID"
  plaintext_value  = octopusdeploy_lifecycle.productiononly_lifecycle.id
}

resource "github_actions_secret" "developmentonly_lifecycle_id" {
  repository       = var.github_repo
  secret_name      = "OCTOPUS_DEVELOPMENT_ONLY_LIFECYCLE_ID"
  plaintext_value  = octopusdeploy_lifecycle.developmentonly_lifecycle.id
}

resource "github_actions_secret" "administration_lifecycle_id" {
  repository       = var.github_repo
  secret_name      = "OCTOPUS_ADMINISTRATION_LIFECYCLE_ID"
  plaintext_value  = octopusdeploy_lifecycle.administration_lifecycle.id
}