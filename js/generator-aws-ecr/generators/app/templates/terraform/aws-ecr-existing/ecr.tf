# Lookup the existing ECR instance and use that as the basis for the Octopus feed
data "aws_ecr_repository" "aws_ecr_repository" {
  name = "${var.aws_ecr_repository_name}-${lower(var.github_repo_owner)}-${lower(var.platform)}"
}

output "aws_ecr_repository_url" {
  value = data.aws_ecr_repository.aws_ecr_repository.repository_url
}

output "aws_ecr_repository_name" {
  value = data.aws_ecr_repository.aws_ecr_repository.name
}