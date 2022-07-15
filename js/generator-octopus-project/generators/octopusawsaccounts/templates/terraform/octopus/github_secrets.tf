resource "github_actions_secret" "development_aws_access_key" {
  repository       = var.github_repo
  secret_name      = "OCTOPUS_AWS_DEVELOPMENT_ACCOUNT_ID"
  plaintext_value  = octopusdeploy_aws_account.development_aws_access_key.id
}

resource "github_actions_secret" "production_aws_access_key" {
  repository       = var.github_repo
  secret_name      = "OCTOPUS_AWS_PRODUCTION_ACCOUNT_ID"
  plaintext_value  = octopusdeploy_aws_account.production_aws_access_key.id
}
