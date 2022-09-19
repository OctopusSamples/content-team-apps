# The AWS secret key used to create S3 buckets for the Terraform state.
resource "github_actions_secret" "terraform_aws_access_key" {
  repository       = var.github_repo
  secret_name      = "AWS_ACCESS_KEY_ID"
  plaintext_value  = var.terraform_aws_access_key
}

# The AWS secret key used to create S3 buckets for the Terraform state.
resource "github_actions_secret" "terraform_aws_secret_access_key" {
  repository       = var.github_repo
  secret_name      = "AWS_SECRET_ACCESS_KEY"
  plaintext_value  = var.terraform_aws_secret_access_key
}

# The Octopus server URL
resource "github_actions_secret" "octopus_server" {
  repository       = var.github_repo
  secret_name      = "OCTOPUS_SERVER"
  plaintext_value  = var.octopus_server
}

# The Octopus server API key
resource "github_actions_secret" "octopus_apikey" {
  repository       = var.github_repo
  secret_name      = "OCTOPUS_APIKEY"
  plaintext_value  = var.octopus_apikey
}

# The Octopus space ID
resource "github_actions_secret" "octopus_space_id" {
  repository       = var.github_repo
  secret_name      = "OCTOPUS_SPACE_ID"
  plaintext_value  = var.octopus_space_id
}

# Subsequent projects will create secrets to capture dynamic variables like account
# IDs, feed IDs etc. The GitHub Actions token provided to workflows does not, and
# can not, have the permissions required to create secrets. A GitHub app is required
# to allow workflows to create secrets.
resource "github_actions_secret" "github_app_id" {
  repository       = var.github_repo
  secret_name      = "GH_APP_ID"
  plaintext_value  = var.github_app_id
}

# The GitHub App installation ID
resource "github_actions_secret" "github_installation_id" {
  repository       = var.github_repo
  secret_name      = "GH_INSTALLATION_ID"
  plaintext_value  = var.github_installation_id
}

# The GitHub App key
resource "github_actions_secret" "github_pem_file" {
  repository       = var.github_repo
  secret_name      = "GH_PEM_FILE"
  plaintext_value  = file(var.github_pem_file)
}

# The dev AWS access key
resource "github_actions_secret" "development_aws_access_key" {
  repository       = var.github_repo
  secret_name      = "AWS_DEVELOPMENT_ACCESS_KEY_ID"
  plaintext_value  = var.development_aws_access_key
}

# The dev AWS secret key
resource "github_actions_secret" "development_aws_secret_access_key" {
  repository       = var.github_repo
  secret_name      = "AWS_DEVELOPMENT_SECRET_ACCESS_KEY_ID"
  plaintext_value  = var.development_aws_secret_access_key
}

# The prod AWS access key
resource "github_actions_secret" "production_aws_access_key" {
  repository       = var.github_repo
  secret_name      = "AWS_PRODUCTION_ACCESS_KEY_ID"
  plaintext_value  = var.production_aws_access_key
}

# The dev AWS secret key
resource "github_actions_secret" "production_aws_secret_access_key" {
  repository       = var.github_repo
  secret_name      = "AWS_PRODUCTION_SECRET_ACCESS_KEY_ID"
  plaintext_value  = var.production_aws_secret_access_key
}

# The Dockerhub username
resource "github_actions_secret" "dockerhub_username" {
  repository       = var.github_repo
  secret_name      = "DOCKERHUB_USERNAME"
  plaintext_value  = var.dockerhub_username
}

# The Dockerhub username
resource "github_actions_secret" "dockerhub_password" {
  repository       = var.github_repo
  secret_name      = "DOCKERHUB_PASSWORD"
  plaintext_value  = var.dockerhub_password
}