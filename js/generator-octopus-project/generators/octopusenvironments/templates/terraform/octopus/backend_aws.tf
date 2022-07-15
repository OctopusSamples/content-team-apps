terraform {
  backend "s3" {
    bucket = "workflow-builder-${PROJECT_NAME}"
    key    = "environments"
    region = "${TERRAFORM_BUCKET_REGION}"
  }
}