resource "octopusdeploy_docker_container_registry" "dockerhub_feed" {
  feed_uri      = "https://index.docker.io"
  name          = "DockerHub"
}

output "octopus_dockerhub_feed_id" {
  value = octopusdeploy_docker_container_registry.dockerhub_feed.id
}

resource "octopusdeploy_docker_container_registry" "github_docker_feed" {
  feed_uri = "https://gchr.io"
  name     = "GitHub Docker"
  password = var.github_feed_token
  username = var.github_username
}

output "octopus_github_docker_feed_id" {
  value = octopusdeploy_docker_container_registry.github_docker_feed.id
}