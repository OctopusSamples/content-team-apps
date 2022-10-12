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

variable "featurebranch_name" {
  type = string
  nullable = false
}

variable "channel_project_name" {
  type = string
  nullable = false
}

variable "step_name" {
  type = string
  nullable = false
}