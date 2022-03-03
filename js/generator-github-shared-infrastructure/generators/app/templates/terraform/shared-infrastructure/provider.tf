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
    bucket = "app-builder-<%= s3_bucket_suffix %>"
    key    = "appbuilder-shared-infrastructure"
    region = "<%= aws_region %>"
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