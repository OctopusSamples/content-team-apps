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

variable "github_repo" {
  type = string
  nullable = false
}

variable "github_owner" {
  type = string
  nullable = false
}

variable "development_app_environment" {
  type = string
  nullable = false
  default = "Development (App)"
}

variable "development_security_environment" {
  type = string
  nullable = false
  default = "Development (Security)"
}

variable "production_app_environment" {
  type = string
  nullable = false
  default = "Production (App)"
}

variable "production_security_environment" {
  type = string
  nullable = false
  default = "Production (Security)"
}

variable "administration_environment" {
  type = string
  nullable = false
  default = "Administration"
}