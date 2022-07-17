terraform {
  backend "s3" {
    bucket = "${GITHUB_OWNER}-${GITHUB_REPO}-${PROJECT_NAME}"
    key    = "appbuilder-apprunner-deployment"
    region = "${TERRAFORM_BUCKET_REGION}"
  }
}