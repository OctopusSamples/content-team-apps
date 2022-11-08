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

variable "namespace_prefix" {
  type = string
  nullable = false
  default = ""
  description = "The prefix of the namespace used to hold the microservice demo. For example, you may set this to you own name to create an isolated deployment in a shared cluster."
}

variable "github_owner" {
  type = string
  nullable = false
}
