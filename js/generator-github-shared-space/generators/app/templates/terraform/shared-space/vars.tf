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

variable "github_repo" {
  type = string
  nullable = false
}

variable "github_repo_owner" {
  type = string
  nullable = false
}