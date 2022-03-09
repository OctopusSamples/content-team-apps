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

variable "octopus_infrastructure_lifecycle_id" {
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