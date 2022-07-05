variable "octopus_server" {
  type = string
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

variable "octopus_project_group_name" {
  type = string
  nullable = false
}

variable "aws_region" {
  type = string
  nullable = false
}

variable "octopus_aws_development_account_id" {
  type = string
  nullable = false
}

variable "octopus_aws_production_account_id" {
  type = string
  nullable = false
}

variable "octopus_lifecycle_id" {
  type = string
  nullable = false
}

variable "cloudformation_stack_name" {
  type = string
  nullable = false
}
