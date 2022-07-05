terraform {
  backend "s3" {
    bucket = "app-builder-d66bfa00-e6d4-48be-89a8-e978f1e5e771"
    key    = "appbuilder-ecr-deployment"
    region = "us-west-1"
  }
}