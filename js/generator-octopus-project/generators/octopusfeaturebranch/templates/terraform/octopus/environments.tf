resource "octopusdeploy_environment" "environment" {
  allow_dynamic_infrastructure = true
  description                  = "Feature branch environment for ${var.featurebranch_name}"
  name                         = var.featurebranch_name
  use_guided_failure           = false
}