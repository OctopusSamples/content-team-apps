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

variable "development_aws_access_key" {
  type = string
  nullable = false
  sensitive = true
  description = "The AWS access key used to create the account for the development environment"
}

variable "development_aws_secret_access_key" {
  type = string
  nullable = false
  sensitive = true
  description = "The AWS secret access key used to create the account for the development environment"
}

variable "production_aws_access_key" {
  type = string
  nullable = false
  sensitive = true
  description = "The AWS access key used to create the account for the production environment"
}

variable "production_aws_secret_access_key" {
  type = string
  nullable = false
  sensitive = true
  description = "The AWS secret access key used to create the account for the production environment"
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