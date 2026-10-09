variable "name" {
  description = "Micro standard name (<capacity>-<country>-<functionality>-<env>) used to name the service resources."
  type        = string
  nullable    = false

  validation {
    condition     = length("tg-${var.name}") <= 32
    error_message = "The target group name tg-<name> must have at most 32 characters."
  }
}

variable "cluster_name" {
  description = "ECS cluster that runs the service."
  type        = string
  nullable    = false
}

variable "vpc_id" {
  description = "VPC of the service."
  type        = string
  nullable    = false
}

variable "subnet_ids" {
  description = "Private subnets where the tasks run."
  type        = list(string)
  nullable    = false
}

variable "listener_arn" {
  description = "Load balancer listener where the routing rule is registered."
  type        = string
  nullable    = false
}

variable "listener_rule_priority" {
  description = "Priority of the routing rule in the listener."
  type        = number
  default     = 100
}

variable "path_patterns" {
  description = "Request paths routed to the service."
  type        = list(string)
  default     = ["/*"]
}

variable "alb_security_group_id" {
  description = "Security group of the load balancer; it is the only source allowed to reach the tasks."
  type        = string
  nullable    = false
}

variable "alb_arn_suffix" {
  description = "ARN suffix of the load balancer, used by the request count scaling policy."
  type        = string
  nullable    = false
}

variable "container_image" {
  description = "Full image reference (repository URL and immutable tag)."
  type        = string
  nullable    = false
}

variable "ecr_repository_arn" {
  description = "Repository the execution role may pull from."
  type        = string
  nullable    = false
}

variable "container_port" {
  description = "Port the application listens on."
  type        = number
  default     = 8080
}

variable "cpu" {
  description = "Task CPU units (1024 = 1 vCPU)."
  type        = number
  default     = 512

  validation {
    condition     = contains([256, 512, 1024, 2048, 4096], var.cpu)
    error_message = "cpu must be a Fargate size: 256, 512, 1024, 2048 or 4096."
  }
}

variable "memory" {
  description = "Task memory in MiB; it must be a valid Fargate combination with cpu."
  type        = number
  default     = 1024
}

variable "environment" {
  description = "Non-sensitive environment variables of the container. Credentials never go here."
  type        = map(string)
  default     = {}
}

variable "secret_arns" {
  description = "Secrets Manager secrets the application reads at runtime through the task role."
  type        = list(string)
  nullable    = false

  validation {
    condition     = length(var.secret_arns) > 0
    error_message = "secret_arns must contain at least one secret."
  }
}

variable "database_security_group_id" {
  description = "Security group of the database; the tasks may open connections only to it."
  type        = string
  nullable    = false
}

variable "database_port" {
  description = "Port of the database."
  type        = number
  default     = 5432
}

variable "health_check_path" {
  description = "Readiness path checked by the load balancer before routing traffic to a task."
  type        = string
  default     = "/actuator/health/readiness"
}

variable "container_health_check_path" {
  description = "Liveness path checked by ECS inside the container."
  type        = string
  default     = "/actuator/health/liveness"
}

variable "health_check_grace_period_seconds" {
  description = "Seconds ECS ignores failing load balancer health checks while a new task starts."
  type        = number
  default     = 90
}

variable "deregistration_delay" {
  description = "Seconds the load balancer keeps draining a task; it matches the application graceful shutdown."
  type        = number
  default     = 20
}

variable "use_fargate_spot" {
  description = "Run the tasks on Fargate Spot (about 70% cheaper, may be interrupted)."
  type        = bool
  default     = false
}

variable "desired_count" {
  description = "Initial number of tasks; afterwards auto scaling owns it."
  type        = number
  default     = 1
}

variable "min_capacity" {
  description = "Minimum number of tasks kept by auto scaling."
  type        = number
  default     = 1
}

variable "max_capacity" {
  description = "Maximum number of tasks auto scaling may run."
  type        = number
  default     = 2

  validation {
    condition     = var.max_capacity >= 1
    error_message = "max_capacity must be at least 1."
  }
}

variable "cpu_target_value" {
  description = "Average CPU utilization (%) auto scaling keeps the service at."
  type        = number
  default     = 60
}

variable "requests_per_target" {
  description = "Load balancer requests per task per minute auto scaling keeps the service at."
  type        = number
  default     = 500
}

variable "scale_out_cooldown" {
  description = "Seconds after a scale out before another scaling activity."
  type        = number
  default     = 60
}

variable "scale_in_cooldown" {
  description = "Seconds after a scale in before another scaling activity."
  type        = number
  default     = 300
}

variable "log_retention_days" {
  description = "Days the container logs are kept in CloudWatch."
  type        = number
  default     = 3

  validation {
    condition     = contains([1, 3, 5, 7, 14, 30, 60, 90, 120, 150, 180, 365], var.log_retention_days)
    error_message = "log_retention_days must be a value accepted by CloudWatch Logs."
  }
}

variable "permissions_boundary_arn" {
  description = "Optional IAM permissions boundary attached to the task and execution roles."
  type        = string
  default     = null
}

variable "wait_for_steady_state" {
  description = "Make terraform apply wait until the new deployment is healthy (or rolled back)."
  type        = bool
  default     = true
}

variable "tags" {
  description = "Additional tags for the service resources."
  type        = map(string)
  default     = {}
}
