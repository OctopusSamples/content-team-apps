terraform {
  backend "s3" {
    bucket = "app-builder-<%= terraform_bucket_suffix %>"
    key    = "appbuilder-apprunner-deployment"
    region = "<%= aws_region %>"
  }
}