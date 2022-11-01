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

variable "octopus_dockerhub_feed_id" {
  type = string
  nullable = false
}

variable "octopus_built_in_feed_id" {
  type = string
  nullable = false
}

variable "octopus_application_lifecycle_id" {
  type = string
  nullable = false
}

variable "octopus_simple_lifecycle_id" {
  type = string
  nullable = false
}

variable "octopus_production_only_lifecycle_id" {
  type = string
  nullable = false
}

variable "octopus_development_app_environment_id" {
  type = string
  nullable = false
}

variable "octopus_development_security_environment_id" {
  type = string
  nullable = false
}

variable "octopus_production_app_environment_id" {
  type = string
  nullable = false
}

variable "octopus_production_security_environment_id" {
  type = string
  nullable = false
}

variable "github_package_pat" {
  type = string
  nullable = false
}
