resource "octopusdeploy_maven_feed" "github_maven_feed" {
  download_attempts              = 10
  download_retry_backoff_seconds = 20
  feed_uri                       = "https://maven.pkg.github.com/OctopusSamples/google-microservices-demo"
  password                       = var.github_package_pat
  name                           = "GitHub Maven Feed"
  username                       = "bob"
}

data "octopusdeploy_feeds" "built-in-feed" {
  feed_type    = "BuiltIn"
  skip         = 0
  take         = 1
}