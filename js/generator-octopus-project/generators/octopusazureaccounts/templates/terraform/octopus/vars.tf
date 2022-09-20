variable "octopus_server" {
  type = string
  nullable = false
}

variable "octopus_apikey" {
  type      = string
  sensitive = true
  nullable = false
}

variable "octopus_space_id" {
  type = string
  nullable = false
}

variable "development_azure_application_id" {
  type = string
  nullable = true
  sensitive = true
  description = "The dev azure account application ID. This is optional if subsequent projects don't deploy anything to Azure."
}

variable "development_azure_password" {
  type = string
  nullable = true
  sensitive = true
  description = "The dev azure account password. This is optional if subsequent projects don't deploy anything to Azure."
}

variable "development_azure_subscription_id" {
  type = string
  nullable = true
  sensitive = true
  description = "The dev azure account subscription id. This is optional if subsequent projects don't deploy anything to Azure."
}

variable "development_azure_tenant_id" {
  type = string
  nullable = true
  sensitive = true
  description = "The dev azure account tenant id. This is optional if subsequent projects don't deploy anything to Azure."
}

variable "production_azure_application_id" {
  type = string
  nullable = true
  sensitive = true
  description = "The production azure account application ID. This is optional if subsequent projects don't deploy anything to Azure."
}

variable "production_azure_password" {
  type = string
  nullable = true
  sensitive = true
  description = "The production azure account password. This is optional if subsequent projects don't deploy anything to Azure."
}

variable "production_azure_subscription_id" {
  type = string
  nullable = true
  sensitive = true
  description = "The production azure account subscription id. This is optional if subsequent projects don't deploy anything to Azure."
}

variable "production_azure_tenant_id" {
  type = string
  nullable = true
  sensitive = true
  description = "The production azure account tenant id. This is optional if subsequent projects don't deploy anything to Azure."
}

variable "development_environment_id" {
  type = string
  nullable = false
}

variable "development_security_environment" {
  type = string
  nullable = false
}

variable "production_environment_id" {
  type = string
  nullable = false
}

variable "production_security_environment" {
  type = string
  nullable = false
}

variable "github_repo" {
  type = string
  nullable = false
}

variable "github_owner" {
  type = string
  nullable = false
}