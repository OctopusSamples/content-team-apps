resource "github_actions_secret" "development_aws_access_key" {
  repository       = var.github_repo
  secret_name      = "OCTOPUS_DOCKERHUB_FEED_ID"
  plaintext_value  = octopusdeploy_docker_container_registry.dockerhub.id
}