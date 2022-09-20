variable "github_repo" {
  type = string
  nullable = false
  description = "The GitHub repo name that is to be populated. This is just the repo name i.e. myrepo in the url https://github.com/owner/myrepo."
}

variable "github_owner" {
  type = string
  nullable = false
  description = "The GitHub repo owner that is to be populated. This is just the repo owner i.e. owner in the url https://github.com/owner/myrepo."
  default = "OctopusDeploy"
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
  nullable = true
  sensitive = true
  description = "The AWS access key used to create the account for the development environment. This is optional if subsequent projects don't deploy anything to AWS."
}

variable "development_aws_secret_access_key" {
  type = string
  nullable = true
  sensitive = true
  description = "The AWS secret access key used to create the account for the development environment. This is optional if subsequent projects don't deploy anything to AWS."
}

variable "production_aws_access_key" {
  type = string
  nullable = true
  sensitive = true
  description = "The AWS access key used to create the account for the production environment. This is optional if subsequent projects don't deploy anything to AWS."
}

variable "production_aws_secret_access_key" {
  type = string
  nullable = true
  sensitive = true
  description = "The AWS secret access key used to create the account for the production environment. This is optional if subsequent projects don't deploy anything to AWS."
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

variable "development_azure_application_id" {
  type = string
  nullable = true
  sensitive = true
  description = "The dev azure account application ID. This is optional if subsequent projects don't deploy anything to Azure."
}

variable "development_azure_password" {
  type = string
  nullable = true
  sensitive = true
  description = "The dev azure account password. This is optional if subsequent projects don't deploy anything to Azure."
}

variable "development_azure_subscription_id" {
  type = string
  nullable = true
  sensitive = true
  description = "The dev azure account subscription id. This is optional if subsequent projects don't deploy anything to Azure."
}

variable "development_azure_tenant_id" {
  type = string
  nullable = true
  sensitive = true
  description = "The dev azure account tenant id. This is optional if subsequent projects don't deploy anything to Azure."
}

variable "production_azure_application_id" {
  type = string
  nullable = true
  sensitive = true
  description = "The production azure account application ID. This is optional if subsequent projects don't deploy anything to Azure."
}

variable "production_azure_password" {
  type = string
  nullable = true
  sensitive = true
  description = "The production azure account password. This is optional if subsequent projects don't deploy anything to Azure."
}

variable "production_azure_subscription_id" {
  type = string
  nullable = true
  sensitive = true
  description = "The production azure account subscription id. This is optional if subsequent projects don't deploy anything to Azure."
}

variable "production_azure_tenant_id" {
  type = string
  nullable = true
  sensitive = true
  description = "The production azure account tenant id. This is optional if subsequent projects don't deploy anything to Azure."
}