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
    region = "<%= aws_state_bucket_region %>"
  }
}