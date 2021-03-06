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

variable "octopus_dockerhub_feed_id" {
  type = string
  nullable = false
}

variable "docker_image" {
  type = string
  nullable = false
}

variable "docker_image_port" {
  type = string
  nullable = false
}

variable "dockerhub_password" {
  type = string
  nullable = false
  sensitive = true
}

variable "dockerhub_username" {
  type = string
  nullable = false
}