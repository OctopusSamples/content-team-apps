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

variable "github_docker_feed_id" {
  type = string
  nullable = false
}

variable "dockerhub_feed_id" {
  type = string
  nullable = false
}