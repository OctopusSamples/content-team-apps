terraform {
  required_providers {
    octopusdeploy = {
      source  = "OctopusDeployLabs/octopusdeploy"
      version = "0.8.1"
    }
    github = {
      source = "integrations/github"
      version = "4.26.1"
    }
  }
}

provider "octopusdeploy" {
  address  = var.octopus_server
  api_key  = var.octopus_apikey
  space_id = var.octopus_space_id
}

provider "github" {
  app_auth {}
}

