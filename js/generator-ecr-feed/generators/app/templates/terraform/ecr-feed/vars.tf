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

variable "aws_access_key" {
  type = string
  nullable = false
}

variable "aws_secret_key" {
  type      = string
  sensitive = true
  nullable = false
}

variable "aws_region" {
  type = string
  nullable = false
}