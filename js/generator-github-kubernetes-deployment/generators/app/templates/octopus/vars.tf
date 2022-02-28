variable "octopus_server" {
  type = string
}

variable "octopus_apikey" {
  type      = string
  sensitive = true
}

variable "octopus_space_id" {
  type = string
}

variable "aws_region" {
  type = string
}