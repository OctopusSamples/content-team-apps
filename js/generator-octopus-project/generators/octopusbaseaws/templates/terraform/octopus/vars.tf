variable "project_name" {
  type = string
  nullable = false
}

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

variable "octopus_development_environment_id" {
  type = string
  nullable = false
}

variable "octopus_production_environment_id" {
  type = string
  nullable = false
}

variable "octopus_development_security_environment_id" {
  type = string
  nullable = false
}

variable "octopus_production_security_environment_id" {
  type = string
  nullable = false
}

variable "octopus_project_name" {
  type = string
  nullable = false
}

variable "octopus_project_description" {
  type = string
  nullable = false
}

variable "octopus_project_group_name" {
  type = string
  nullable = false
}

variable "existing_project_group" {
  type = bool
  nullable = false
}

variable "octopus_lifecycle_id" {
  type = string
  nullable = false
}