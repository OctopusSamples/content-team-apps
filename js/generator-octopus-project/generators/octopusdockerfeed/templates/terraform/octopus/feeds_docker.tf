resource "octopusdeploy_docker_container_registry" "dockerhub" {
  feed_uri      = "https://index.docker.io"
  name          = "DockerHub"
}