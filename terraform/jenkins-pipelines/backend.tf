terraform {
  backend "s3" {
    bucket = "app-builder-c81b45ae-50de-466c-8500-3845fd8b80c4"
    key    = "jenkins-pipelines"
    region = "us-west-1"
  }
}