terraform {
  backend "s3" {
    bucket = "workflow-builder-${GITHUB_OWNER}-${GITHUB_REPO}-${PROJECT_NAME}"
    key    = "awsaccounts"
    region = "${TERRAFORM_BUCKET_REGION}"
  }
}