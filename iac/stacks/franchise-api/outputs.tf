output "api_url" {
  description = "Base URL of the API through the load balancer."
  value       = local.api_url
}

output "swagger_url" {
  description = "Swagger UI of the deployed API."
  value       = "${local.api_url}/swagger-ui.html"
}

output "service_name" {
  description = "ECS service name."
  value       = module.ecs_service.service_name
}

output "log_group_name" {
  description = "CloudWatch log group with the application logs."
  value       = module.ecs_service.log_group_name
}

output "task_role_arn" {
  description = "Role assumed by the application."
  value       = module.ecs_service.task_role_arn
}
