output "service_name" {
  description = "Name of the ECS service."
  value       = aws_ecs_service.this.name
}

output "task_definition_arn" {
  description = "ARN of the task definition revision in use."
  value       = aws_ecs_task_definition.this.arn
}

output "task_role_arn" {
  description = "Role assumed by the application."
  value       = aws_iam_role.task.arn
}

output "execution_role_arn" {
  description = "Role used by ECS to pull the image and write logs."
  value       = aws_iam_role.execution.arn
}

output "security_group_id" {
  description = "Security group of the tasks."
  value       = aws_security_group.service.id
}

output "log_group_name" {
  description = "CloudWatch log group of the containers."
  value       = aws_cloudwatch_log_group.this.name
}
