terraform {
  required_providers {
    octopusdeploy = {
      source  = "OctopusDeployLabs/octopusdeploy"
      version = "~> 0.7.68"
    }
    aws = {
      source = "hashicorp/aws"
      version = "4.3.0"
    }
  }

  backend "s3" {
    bucket = "app-builder-c81b45ae-50de-466c-8500-3845fd8b80c"
    key    = "app-builder-frontend"
    region = "us-west-1"
  }
}