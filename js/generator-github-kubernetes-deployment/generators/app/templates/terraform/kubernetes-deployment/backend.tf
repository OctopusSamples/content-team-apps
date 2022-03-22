terraform {
  backend "s3" {
    bucket = "app-builder-<%= s3_bucket_suffix %>"
    key    = "appbuilder-kubernetes-deployment"
    region = "<%= aws_state_bucket_region %>"
  }
}