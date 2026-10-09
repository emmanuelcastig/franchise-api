output "repository_name" {
  description = "Name of the repository."
  value       = aws_ecr_repository.this.name
}

output "repository_url" {
  description = "URL used to tag and push images."
  value       = aws_ecr_repository.this.repository_url
}

output "repository_arn" {
  description = "ARN of the repository, used to scope the image pull permissions."
  value       = aws_ecr_repository.this.arn
}
