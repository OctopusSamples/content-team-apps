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

variable "github_docker_feed_id" {
  type = string
}

variable "dockerhub_feed_id" {
  type = string
}