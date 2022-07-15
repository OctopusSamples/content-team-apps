terraform {
  backend "s3" {
    bucket = "workflow-builder-${PROJECT_NAME}"
    key    = "appbuilder-ecr-deployment"
    region = "${TERRAFORM_BUCKET_REGION}"
  }
}