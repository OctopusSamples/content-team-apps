resource "octopusdeploy_docker_container_registry" "dockerhub" {
  feed_uri = "https://index.docker.io"
  name     = var.dockerhub_feed_name
  username = var.dockerhub_username
  password = var.dockerhub_password
  count    = var.existing_dockerhub_feed ? 0 : 1
}

data "octopusdeploy_feeds" "dockerhub" {
  feed_type    = "Docker"
  partial_name = var.dockerhub_feed_name
  skip         = 0
  take         = 1
}