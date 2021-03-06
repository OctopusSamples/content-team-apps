variable "github_repo" {
  type = string
  nullable = false
  description = "The GitHub repo name that is to be populated"
}

variable "github_owner" {
  type = string
  nullable = false
  description = "The GitHub repo owner that is to be populated"
}

variable "github_token" {
  type = string
  nullable = false
  sensitive = true
  description = "The GitHub access token"
}

variable "octopus_server" {
  type = string
  nullable = false
  description = "The Octopus server that additional generators populate"
}

variable "octopus_apikey" {
  type      = string
  sensitive = true
  nullable = false
  description = "The Octopus API key"
}

variable "octopus_space_id" {
  type = string
  nullable = false
  description = "The Octopus space ID"
}

variable "terraform_aws_access_key" {
  type = string
  nullable = false
  sensitive = true
  description = "The AWS access key used to access the S3 bucket holding the Terraform state"
}

variable "terraform_aws_secret_access_key" {
  type = string
  nullable = false
  sensitive = true
  description = "The AWS secret access key used to access the S3 bucket holding the Terraform state"
}

variable "development_aws_access_key" {
  type = string
  nullable = false
  sensitive = true
  description = "The AWS access key used to create the account for the development environment"
}

variable "development_aws_secret_access_key" {
  type = string
  nullable = false
  sensitive = true
  description = "The AWS secret access key used to create the account for the development environment"
}

variable "production_aws_access_key" {
  type = string
  nullable = false
  sensitive = true
  description = "The AWS access key used to create the account for the production environment"
}

variable "production_aws_secret_access_key" {
  type = string
  nullable = false
  sensitive = true
  description = "The AWS secret access key used to create the account for the production environment"
}

variable "github_app_id" {
  type = string
  nullable = false
  sensitive = false
  description = "The GitHub app ID used when terraform saves secrets as part of a GitHub action"
}

variable "github_installation_id" {
  type = string
  nullable = false
  sensitive = false
  description = "The GitHub installation ID used when terraform saves secrets as part of a GitHub action"
}

variable "github_pem_file" {
  type = string
  nullable = false
  sensitive = false
  description = "The path to the GitHub private key used when terraform saves secrets as part of a GitHub action"
}


variable "dockerhub_username" {
  type = string
  nullable = false
  sensitive = false
  description = "The DockerHub username"
}

variable "dockerhub_password" {
  type = string
  nullable = false
  sensitive = true
  description = "The DockerHub password"
}