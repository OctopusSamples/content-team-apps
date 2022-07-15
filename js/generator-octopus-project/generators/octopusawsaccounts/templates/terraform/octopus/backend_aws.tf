terraform {
  backend "s3" {
    bucket = "workflow-builder-${PROJECT_NAME}"
    key    = "awsaccounts"
    region = "${TERRAFORM_BUCKET_REGION}"
  }
}