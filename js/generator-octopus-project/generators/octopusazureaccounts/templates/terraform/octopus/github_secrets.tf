resource "github_actions_secret" "development_azure_account" {
  count           = var.development_azure_application_id != "" ? 1 : 0
  repository      = var.github_repo
  secret_name     = "OCTOPUS_AZURE_DEVELOPMENT_ACCOUNT_ID"
  plaintext_value = octopusdeploy_azure_service_principal.development_azure_account[0].id
}

resource "github_actions_secret" "production_aws_access_key" {
  count           = var.production_azure_application_id != "" ? 1 : 0
  repository      = var.github_repo
  secret_name     = "OCTOPUS_AZURE_PRODUCTION_ACCOUNT_ID"
  plaintext_value = octopusdeploy_azure_service_principal.production_azure_account[0].id
}
