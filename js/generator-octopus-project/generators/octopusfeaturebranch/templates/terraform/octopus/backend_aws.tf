terraform {
  backend "s3" {
    bucket = "${GITHUB_OWNER}-${GITHUB_REPO}-${PROJECT_NAME}-${FEATURE_BRANCH}"
    key    = "octopusfeaturebranch"
    region = "${TERRAFORM_BUCKET_REGION}"
  }
}