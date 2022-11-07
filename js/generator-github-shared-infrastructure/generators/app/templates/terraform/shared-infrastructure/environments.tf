resource "octopusdeploy_environment" "development_environment" {
  allow_dynamic_infrastructure = true
  description                  = "An environment for the development team. This resource is created and managed by the [Octopus Terraform provider](https://registry.terraform.io/providers/OctopusDeployLabs/octopusdeploy/latest/docs). The Terraform files can be found in the [GitHub repo](https://github.com/${var.github_repo})."
  name                         = "Development"
  use_guided_failure           = false
}

output "development_environment_id" {
  value = octopusdeploy_environment.development_environment.id
}

resource "octopusdeploy_environment" "development_security_environment" {
  allow_dynamic_infrastructure = true
  description                  = "Used to scan the development releases for security issues. This resource is created and managed by the [Octopus Terraform provider](https://registry.terraform.io/providers/OctopusDeployLabs/octopusdeploy/latest/docs). The Terraform files can be found in the [GitHub repo](https://github.com/${var.github_repo})."
  name                         = "Development (Security)"
  use_guided_failure           = false
  depends_on = [octopusdeploy_environment.development_environment]
}

output "development_security_environment_id" {
  value = octopusdeploy_environment.development_security_environment.id
}

resource "octopusdeploy_environment" "production_environment" {
  allow_dynamic_infrastructure = true
  description                  = "The production environment."
  name                         = "Production"
  use_guided_failure           = false
  depends_on = [octopusdeploy_environment.development_security_environment]
}

output "production_environment_id" {
  value = octopusdeploy_environment.production_environment.id
}

resource "octopusdeploy_environment" "production_security_environment" {
  allow_dynamic_infrastructure = true
  description                  = "Used to scan the productions releases for security issues. This resource is created and managed by the [Octopus Terraform provider](https://registry.terraform.io/providers/OctopusDeployLabs/octopusdeploy/latest/docs). The Terraform files can be found in the [GitHub repo](https://github.com/${var.github_repo})."
  name                         = "Production (Security)"
  use_guided_failure           = false
  depends_on = [octopusdeploy_environment.production_environment]
}

output "production_security_environment_id" {
  value = octopusdeploy_environment.production_security_environment.id
}

resource "octopusdeploy_environment" "administration_environment" {
  allow_dynamic_infrastructure = true
  description                  = "Used for cross cutting administration concerns. This resource is created and managed by the [Octopus Terraform provider](https://registry.terraform.io/providers/OctopusDeployLabs/octopusdeploy/latest/docs). The Terraform files can be found in the [GitHub repo](https://github.com/${var.github_repo})."
  name                         = "Administration"
  use_guided_failure           = false
  depends_on = [octopusdeploy_environment.production_security_environment]
}

output "administration_environment_id" {
  value = octopusdeploy_environment.administration_environment.id
}