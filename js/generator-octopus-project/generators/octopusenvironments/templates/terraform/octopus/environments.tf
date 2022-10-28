data "octopusdeploy_environments" "development_security" {
  name = var.octopus_development_security_environment_name
  skip = 0
  take = 1
}

data "octopusdeploy_environments" "production_security" {
  name = var.octopus_production_security_environment_name
  skip = 0
  take = 1
}

data "octopusdeploy_environments" "development" {
  name = var.octopus_development_app_environment_name
  skip = 0
  take = 1
}

data "octopusdeploy_environments" "production" {
  name = var.octopus_production_app_environment_name
  skip = 0
  take = 1
}

data "octopusdeploy_environments" "administration" {
  name = var.octopus_administration_environment_name
  skip = 0
  take = 1
}

resource "octopusdeploy_environment" "development_environment" {
  allow_dynamic_infrastructure = true
  description                  = "An environment for the development team. This resource is created and managed by the [Octopus Terraform provider](https://registry.terraform.io/providers/OctopusDeployLabs/octopusdeploy/latest/docs). The Terraform files can be found in the [GitHub repo](https://github.com/${var.github_repo})."
  name                         = var.octopus_development_app_environment_name
  use_guided_failure           = false
  count                        = var.octopus_development_app_environment_exists ? 0 : 1
}

resource "octopusdeploy_environment" "development_security_environment" {
  allow_dynamic_infrastructure = true
  description                  = "Used to scan the development releases for security issues. This resource is created and managed by the [Octopus Terraform provider](https://registry.terraform.io/providers/OctopusDeployLabs/octopusdeploy/latest/docs)."
  name                         = var.octopus_development_security_environment_name
  use_guided_failure           = false
  depends_on                   = [octopusdeploy_environment.development_environment]
  count                        = var.octopus_development_security_environment_exists ? 0 : 1
}

resource "octopusdeploy_environment" "production_environment" {
  allow_dynamic_infrastructure = true
  description                  = "The production environment."
  name                         = var.octopus_production_app_environment_name
  use_guided_failure           = false
  depends_on                   = [octopusdeploy_environment.development_security_environment]
  count                        = var.octopus_production_app_environment_exists ? 0 : 1
}

resource "octopusdeploy_environment" "production_security_environment" {
  allow_dynamic_infrastructure = true
  description                  = "Used to scan the productions releases for security issues. This resource is created and managed by the [Octopus Terraform provider](https://registry.terraform.io/providers/OctopusDeployLabs/octopusdeploy/latest/docs)."
  name                         = var.octopus_production_security_environment_name
  use_guided_failure           = false
  depends_on                   = [octopusdeploy_environment.production_environment]
  count                        = var.octopus_production_security_environment_exists ? 0 : 1
}

resource "octopusdeploy_environment" "administration_environment" {
  allow_dynamic_infrastructure = true
  description                  = "Used for cross cutting administration concerns. This resource is created and managed by the [Octopus Terraform provider](https://registry.terraform.io/providers/OctopusDeployLabs/octopusdeploy/latest/docs)."
  name                         = "Administration"
  use_guided_failure           = false
  depends_on                   = [octopusdeploy_environment.production_security_environment]
  count                        = var.octopus_administration_environment_exists ? 0 : 1
}