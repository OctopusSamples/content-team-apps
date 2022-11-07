resource "github_actions_secret" "dockerhub_feed" {
  repository      = var.github_repo
  secret_name     = "OCTOPUS_DOCKERHUB_FEED_ID"
  plaintext_value = octopusdeploy_docker_container_registry.dockerhub[0].id
  count           = var.existing_dockerhub_feed ? 0 : 1
}

resource "github_actions_secret" "dockerhub_feed_existing" {
  repository      = var.github_repo
  secret_name     = "OCTOPUS_DOCKERHUB_FEED_ID"
  plaintext_value = data.octopusdeploy_feeds.dockerhub.feeds[0].id
  count           = var.existing_dockerhub_feed ? 1 : 0
}