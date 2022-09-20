resource "octopusdeploy_azure_service_principal" "development_azure_account" {
  count           = var.development_azure_application_id != "" ? 1 : 0
  application_id  = var.development_azure_application_id
  name            = "Azure Development"
  password        = var.development_azure_password
  subscription_id = var.development_azure_subscription_id
  tenant_id       = var.development_azure_tenant_id
  environments    = [var.development_environment_id, var.development_security_environment]
}

resource "octopusdeploy_azure_service_principal" "production_azure_account" {
  count           = var.production_azure_application_id != "" ? 1 : 0
  application_id  = var.production_azure_application_id
  name            = "Azure Production"
  password        = var.production_azure_password
  subscription_id = var.production_azure_subscription_id
  tenant_id       = var.production_azure_tenant_id
  environments    = [var.production_environment_id, var.production_security_environment]
}