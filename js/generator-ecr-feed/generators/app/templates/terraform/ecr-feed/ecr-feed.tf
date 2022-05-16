resource "octopusdeploy_aws_elastic_container_registry" "ecr_feed" {
  access_key = var.aws_access_key
  name       = "ECR"
  region     = var.aws_region
  secret_key = var.aws_secret_key
}

output "ecr_feed_id" {
  value = octopusdeploy_aws_elastic_container_registry.ecr_feed.id
}