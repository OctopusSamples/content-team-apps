resource "octopusdeploy_aws_account" "aws_account" {
  name         = "AWS Account"
  access_key   = var.aws_access_key
  secret_key   = var.aws_secret_key
}

output "aws_account_id" {
  value = octopusdeploy_aws_account.aws_account.id
}
