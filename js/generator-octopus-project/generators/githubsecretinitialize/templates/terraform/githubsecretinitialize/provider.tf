terraform {
  required_providers {
    github = {
      source = "integrations/github"
      version = "4.26.1"
    }
  }
}

provider "github" {
  app_auth {
    id              = var.github_app_id
    installation_id = var.github_installation_id
    pem_file        = var.github_pem_file
  }
}

