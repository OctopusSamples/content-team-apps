terraform {
  backend "s3" {
    bucket = "${GITHUB_OWNER}-${GITHUB_REPO}-${PROJECT_NAME}"
    key    = "k8sfrontend"
    region = "${TERRAFORM_BUCKET_REGION}"
  }
}