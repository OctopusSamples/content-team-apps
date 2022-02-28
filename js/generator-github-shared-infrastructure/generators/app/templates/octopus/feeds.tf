resource "octopusdeploy_docker_container_registry" "dockerhub_feed" {
  feed_uri      = "https://index.docker.io"
  name          = "DockerHub"
}

resource "octopusdeploy_docker_container_registry" "github_docker_feed" {
  feed_uri = "https://gchr.io"
  name     = "GitHub Docker"
  password = var.github_feed_token
  username = var.github_username
}

resource "octopusdeploy_maven_feed" "github_maven_feed" {
  download_attempts              = 10
  download_retry_backoff_seconds = 20
  feed_uri                       = "https://maven.pkg.github.com/${var.github_organization}/${var.github_repo}"
  password                       = var.github_feed_token
  name                           = "GitHub Maven"
  username                       = var.github_username
}