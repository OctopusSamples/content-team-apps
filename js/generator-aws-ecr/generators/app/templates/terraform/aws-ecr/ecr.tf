resource "aws_ecr_repository" "aws_ecr_repository" {
  name                 = "${var.aws_ecr_repository_name}-${lower(var.github_repo_owner)}-${lower(var.platform)}"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}

output "aws_ecr_repository_url" {
  value = aws_ecr_repository.aws_ecr_repository.repository_url
}

output "aws_ecr_repository_name" {
  value = aws_ecr_repository.aws_ecr_repository.name
}