output "vpc_id" {
  description = "VPC of the environment."
  value       = module.network.vpc_id
}

output "ecs_cluster_name" {
  description = "ECS cluster of the environment."
  value       = module.ecs_cluster.cluster_name
}

output "alb_dns_name" {
  description = "Public DNS name of the load balancer."
  value       = module.alb.dns_name
}

output "service_listener_port" {
  description = "Port of the listener where the services register their routing rules."
  value       = module.alb.service_listener_port
}

output "database_address" {
  description = "Host name of the database."
  value       = module.database.address
}

output "database_secret_arn" {
  description = "RDS-managed secret with the database credentials."
  value       = module.database.master_user_secret_arn
}

output "ecr_repository_url" {
  description = "Repository the service image is pushed to."
  value       = module.ecr.repository_url
}
