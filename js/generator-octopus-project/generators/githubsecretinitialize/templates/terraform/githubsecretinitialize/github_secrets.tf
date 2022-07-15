resource "github_actions_secret" "terraform_aws_access_key" {
  repository       = var.github_repo
  secret_name      = "AWS_ACCESS_KEY_ID"
  plaintext_value  = var.terraform_aws_access_key
}

resource "github_actions_secret" "terraform_aws_secret_access_key" {
  repository       = var.github_repo
  secret_name      = "AWS_SECRET_ACCESS_KEY"
  plaintext_value  = var.terraform_aws_secret_access_key
}

resource "github_actions_secret" "octopus_server" {
  repository       = var.github_repo
  secret_name      = "OCTOPUS_SERVER"
  plaintext_value  = var.octopus_server
}

resource "github_actions_secret" "octopus_apikey" {
  repository       = var.github_repo
  secret_name      = "OCTOPUS_APIKEY"
  plaintext_value  = var.octopus_apikey
}

resource "github_actions_secret" "octopus_space_id" {
  repository       = var.github_repo
  secret_name      = "OCTOPUS_SPACE_ID"
  plaintext_value  = var.octopus_space_id
}

resource "github_actions_secret" "github_app_id" {
  repository       = var.github_repo
  secret_name      = "GH_APP_ID"
  plaintext_value  = var.github_app_id
}

resource "github_actions_secret" "github_installation_id" {
  repository       = var.github_repo
  secret_name      = "GH_INSTALLATION_ID"
  plaintext_value  = var.github_installation_id
}

resource "github_actions_secret" "github_pem_file" {
  repository       = var.github_repo
  secret_name      = "GH_PEM_FILE"
  plaintext_value  = file(var.github_pem_file)
}

resource "github_actions_secret" "development_aws_access_key" {
  repository       = var.github_repo
  secret_name      = "AWS_DEVELOPMENT_ACCESS_KEY_ID"
  plaintext_value  = var.development_aws_access_key
}

resource "github_actions_secret" "development_aws_secret_access_key" {
  repository       = var.github_repo
  secret_name      = "AWS_DEVELOPMENT_SECRET_ACCESS_KEY_ID"
  plaintext_value  = var.development_aws_secret_access_key
}

resource "github_actions_secret" "production_aws_access_key" {
  repository       = var.github_repo
  secret_name      = "AWS_PRODUCTION_ACCESS_KEY_ID"
  plaintext_value  = var.production_aws_access_key
}

resource "github_actions_secret" "production_aws_secret_access_key" {
  repository       = var.github_repo
  secret_name      = "AWS_PRODUCTION_SECRET_ACCESS_KEY_ID"
  plaintext_value  = var.production_aws_secret_access_key
}