resource "octopusdeploy_aws_account" "aws_account" {
  name         = "AWS Account"
  access_key   = var.aws_access_key
  secret_key   = var.aws_secret_key
}