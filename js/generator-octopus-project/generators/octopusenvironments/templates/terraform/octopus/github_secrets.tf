# If we created the environment, save the new ID
resource "github_actions_secret" "development_environment" {
  repository      = var.github_repo
  secret_name     = "OCTOPUS_DEVELOPMENT_ENVIRONMENT_ID"
  plaintext_value = octopusdeploy_environment.development_environment[0].id
  count           = var.octopus_development_app_environment_exists ? 0 : 1
}

# If we reused an environment, save the existing ID
resource "github_actions_secret" "development_environment_existing" {
  repository      = var.github_repo
  secret_name     = "OCTOPUS_DEVELOPMENT_ENVIRONMENT_ID"
  plaintext_value = data.octopusdeploy_environments.development.environments[0].id
  count           = var.octopus_development_app_environment_exists ? 1 : 0
}

resource "github_actions_secret" "development_security_environment" {
  repository      = var.github_repo
  secret_name     = "OCTOPUS_DEVELOPMENT_SECURITY_ENVIRONMENT_ID"
  plaintext_value = octopusdeploy_environment.development_security_environment[0].id
  count           = var.octopus_development_security_environment_exists ? 0 : 1
}

resource "github_actions_secret" "development_security_environment_existing" {
  repository      = var.github_repo
  secret_name     = "OCTOPUS_DEVELOPMENT_SECURITY_ENVIRONMENT_ID"
  plaintext_value = data.octopusdeploy_environments.development_security.environments[0].id
  count           = var.octopus_development_security_environment_exists ? 1 : 0
}

resource "github_actions_secret" "production_environment" {
  repository      = var.github_repo
  secret_name     = "OCTOPUS_PRODUCTION_ENVIRONMENT_ID"
  plaintext_value = octopusdeploy_environment.production_environment[0].id
  count           = var.octopus_production_app_environment_exists ? 0 : 1
}

resource "github_actions_secret" "production_environment_existing" {
  repository      = var.github_repo
  secret_name     = "OCTOPUS_PRODUCTION_ENVIRONMENT_ID"
  plaintext_value = data.octopusdeploy_environments.production.environments[0].id
  count           = var.octopus_production_app_environment_exists ? 1 : 0
}

resource "github_actions_secret" "production_security_environment" {
  repository      = var.github_repo
  secret_name     = "OCTOPUS_PRODUCTION_SECURITY_ENVIRONMENT_ID"
  plaintext_value = octopusdeploy_environment.development_security_environment[0].id
  count           = var.octopus_production_security_environment_exists ? 0 : 1
}

resource "github_actions_secret" "production_security_environment_existing" {
  repository      = var.github_repo
  secret_name     = "OCTOPUS_PRODUCTION_SECURITY_ENVIRONMENT_ID"
  plaintext_value = data.octopusdeploy_environments.production_security.environments[0].id
  count           = var.octopus_production_security_environment_exists ? 1 : 0
}

resource "github_actions_secret" "simple_lifecycle_id" {
  repository      = var.github_repo
  secret_name     = "OCTOPUS_SIMPLE_LIFECYCLE_ID"
  plaintext_value = octopusdeploy_lifecycle.simple_lifecycle.id
}

resource "github_actions_secret" "application_lifecycle_id" {
  repository      = var.github_repo
  secret_name     = "OCTOPUS_APPLICATION_LIFECYCLE_ID"
  plaintext_value = octopusdeploy_lifecycle.application_lifecycle.id
}

resource "github_actions_secret" "productiononly_lifecycle_id" {
  repository      = var.github_repo
  secret_name     = "OCTOPUS_PRODUCTION_ONLY_LIFECYCLE_ID"
  plaintext_value = octopusdeploy_lifecycle.productiononly_lifecycle.id
}

resource "github_actions_secret" "developmentonly_lifecycle_id" {
  repository      = var.github_repo
  secret_name     = "OCTOPUS_DEVELOPMENT_ONLY_LIFECYCLE_ID"
  plaintext_value = octopusdeploy_lifecycle.developmentonly_lifecycle.id
}

resource "github_actions_secret" "administration_lifecycle_id" {
  repository      = var.github_repo
  secret_name     = "OCTOPUS_ADMINISTRATION_LIFECYCLE_ID"
  plaintext_value = octopusdeploy_lifecycle.administration_lifecycle.id
}