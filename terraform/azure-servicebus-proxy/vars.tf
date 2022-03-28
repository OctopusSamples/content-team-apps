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
}

variable "aws_region" {
  type = string
  nullable = false
}

variable "octopus_application_lifecycle_id" {
  type = string
  nullable = false
}

variable "octopus_development_aws_account_id" {
  type = string
  nullable = false
}

variable "octopus_worker_pool_id" {
  type = string
  nullable = false
}

variable "octopus_built_in_feed_id" {
  type = string
  nullable = false
}

variable "octopus_content_team_maven_feed_id" {
  type = string
  nullable = false
}

variable "octopus_development_environment_id" {
  type = string
  nullable = false
}

variable "octopus_production_aws_account_id" {
  type = string
  nullable = false
}

variable "octopus_development_security_environment_id" {
  type = string
  nullable = false
}

variable "octopus_production_environment_id" {
  type = string
  nullable = false
}

variable "octopus_production_security_environment_id" {
  type = string
  nullable = false
}

variable "servicebus_application_id_production" {
  type = string
  nullable = false
  sensitive = true
}

variable "servicebus_application_id_development" {
  type = string
  nullable = false
  sensitive = true
}

variable "servicebus_secret_production" {
  type = string
  nullable = false
  sensitive = true
}

variable "servicebus_secret_development" {
  type = string
  nullable = false
  sensitive = true
}

variable "servicebus_tenant_production" {
  type = string
  nullable = false
  sensitive = true
}

variable "servicebus_tenant_development" {
  type = string
  nullable = false
  sensitive = true
}

variable "servicebus_namespace_production" {
  type = string
  nullable = false
  sensitive = true
}

variable "servicebus_namespace_development" {
  type = string
  nullable = false
  sensitive = true
}

variable "cognito_library_variable_set_id" {
  type = string
  nullable = false
}

variable "content_team_library_variable_set_id" {
  type = string
  nullable = false
}

variable "github_actions_library_variable_set_id" {
  type = string
  nullable = false
}