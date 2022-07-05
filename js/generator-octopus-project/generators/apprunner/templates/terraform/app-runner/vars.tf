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

variable "octopus_project_name" {
  type = string
  nullable = false
}

variable "octopus_project_group_name" {
  type = string
  nullable = false
}

variable "octopus_built_in_feed_id" {
  type = string
  nullable = false
}

variable "octopus_ecr_feed_id" {
  type = string
  nullable = false
}

variable "docker_image" {
  type = string
  nullable = false
}

variable "aws_region" {
  type = string
  nullable = false
}

variable "octopus_aws_account_id" {
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
