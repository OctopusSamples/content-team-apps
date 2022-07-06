terraform {
  backend "s3" {
    bucket = "app-builder-63d7a656-ca63-4fc5-8364-f6e977aa2bd9"
    key    = "appbuilder-ecr-deployment"
    region = "us-west-1"
  }
}