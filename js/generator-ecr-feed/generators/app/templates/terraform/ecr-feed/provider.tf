terraform {
  required_providers {
    octopusdeploy = {
      source  = "OctopusDeployLabs/octopusdeploy"
      version = "0.9.1"
    }
    aws = {
      source = "hashicorp/aws"
      version = "4.3.0"
    }
  }
}

provider "octopusdeploy" {
  address  = var.octopus_server
  api_key  = var.octopus_apikey
  space_id = var.octopus_space_id
}

provider "aws" {
  region = var.aws_region
}
