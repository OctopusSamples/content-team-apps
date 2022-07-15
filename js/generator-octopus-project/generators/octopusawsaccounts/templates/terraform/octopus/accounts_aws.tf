resource "octopusdeploy_aws_account" "development_aws_access_key" {
  name         = "AWS Development"
  access_key   = var.development_aws_access_key
  secret_key   = var.development_aws_secret_access_key
  environments = [var.development_environment_id, var.development_security_environment]
}

resource "octopusdeploy_aws_account" "production_aws_access_key" {
  name         = "AWS Production"
  access_key   = var.production_aws_access_key
  secret_key   = var.production_aws_secret_access_key
  environments = [var.production_environment_id, var.production_security_environment]
}