terraform {
  backend "s3" {
    bucket = "app-builder-c81b45ae-50de-466c-8500-3845fd8b80c4"
    key    = "app-builder-github-oauth-proxy"
    region = "us-west-1"
  }
}