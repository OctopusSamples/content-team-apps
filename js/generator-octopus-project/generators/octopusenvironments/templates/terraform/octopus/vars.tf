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

variable "octopus_development_app_environment_name" {
  type = string
  nullable = false
  default = "Development (App)"
}

variable "octopus_development_security_environment_name" {
  type = string
  nullable = false
  default = "Development (Security)"
}

variable "octopus_production_app_environment_name" {
  type = string
  nullable = false
  default = "Production (App)"
}

variable "octopus_production_security_environment_name" {
  type = string
  nullable = false
  default = "Production (Security)"
}

variable "octopus_administration_environment_name" {
  type = string
  nullable = false
  default = "Administration"
}

variable "octopus_development_app_environment_exists" {
  type = bool
  nullable = false
  default = false
}

variable "octopus_development_security_environment_exists" {
  type = bool
  nullable = false
  default = false
}

variable "octopus_production_app_environment_exists" {
  type = bool
  nullable = false
  default = false
}

variable "octopus_production_security_environment_exists" {
  type = bool
  nullable = false
  default = false
}

variable "octopus_administration_environment_exists" {
  type = bool
  nullable = false
  default = false
}