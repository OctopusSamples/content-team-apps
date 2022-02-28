variable "octopus_server" {
  type = string
}

variable "octopus_apikey" {
  type      = string
  sensitive = true
}

variable "octopus_space" {
  type = string
}

variable "octopus_user_id" {
  type = string
}