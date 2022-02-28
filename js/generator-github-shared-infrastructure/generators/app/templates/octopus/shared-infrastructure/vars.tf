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

variable "github_feed_token" {
  type      = string
  sensitive = true
}

variable "github_username" {
  type = string
}

variable "github_repo" {
  type = string
}

variable "aws_access_key" {
  type = string
}

variable "aws_secret_key" {
  type      = string
  sensitive = true
}

variable "aws_region" {
  type = string
}