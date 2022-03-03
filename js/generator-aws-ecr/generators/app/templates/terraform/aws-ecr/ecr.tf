resource "aws_ecr_repository" "aws_ecr_repository" {
  name                 = "octopus-java-microservice"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}

output "aws_ecr_repository_url" {
  value = aws_ecr_repository.aws_ecr_repository.repository_url
}