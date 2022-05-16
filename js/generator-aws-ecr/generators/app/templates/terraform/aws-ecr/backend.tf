terraform {
  backend "s3" {
    bucket = "app-builder-<%= s3_bucket_suffix %>"
    key    = "appbuilder-generator-aws-ecr-<%= repository %>"
    region = "<%= aws_state_bucket_region %>"
  }
}