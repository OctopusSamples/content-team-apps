variable "octopus_server" {
  type = string
  nullable = false
}

variable "octopus_apikey" {
  type      = string
  sensitive = true
  nullable = false
}

variable "octopus_space" {
  type = string
  nullable = false
}

variable "octopus_user_id" {
  type = string
  nullable = false
}