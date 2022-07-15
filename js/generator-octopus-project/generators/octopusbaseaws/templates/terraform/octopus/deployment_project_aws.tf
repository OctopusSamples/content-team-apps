resource "octopusdeploy_variable" "aws_development_account_deploy_project" {
  name     = "AWS.Account"
  type     = "AmazonWebServicesAccount"
  value    = var.octopus_aws_development_account_id
  owner_id = octopusdeploy_project.deploy_project.id
  scope {
    environments = [
      var.octopus_development_environment_id,
      var.octopus_development_security_environment_id,
    ]
  }
}

resource "octopusdeploy_variable" "aws_production_account_deploy_project" {
  name     = "AWS.Account"
  type     = "AmazonWebServicesAccount"
  value    = var.octopus_aws_production_account_id
  owner_id = octopusdeploy_project.deploy_project.id
  scope {
    environments = [
      var.octopus_production_environment_id,
      var.octopus_production_security_environment_id,
    ]
  }
}