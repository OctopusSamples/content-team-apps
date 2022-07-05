terraform {
  backend "s3" {
    bucket = "app-builder-8e52b6c5-de6d-4b9b-ba20-7c6cf96146e4"
    key    = "appbuilder-apprunner-deployment"
    region = "us-west-1"
  }
}