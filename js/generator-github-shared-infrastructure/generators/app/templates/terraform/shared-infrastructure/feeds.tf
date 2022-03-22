resource "octopusdeploy_docker_container_registry" "dockerhub_feed" {
  feed_uri      = "https://index.docker.io"
  name          = "DockerHub"
}

output "octopus_dockerhub_feed_id" {
  value = octopusdeploy_docker_container_registry.dockerhub_feed.id
}