terraform {
  backend "s3" {
    bucket = "app-builder-<%= terraform_bucket_suffix %>"
    key    = "appbuilder-ecr-deployment"
    region = "<%= aws_region %>"
  }
}