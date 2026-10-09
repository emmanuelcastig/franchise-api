variable "env" {
  description = "Environment where the service is deployed."
  type        = string
  nullable    = false

  validation {
    condition     = contains(["dev", "qa", "pdn"], var.env)
    error_message = "env must be one of: dev, qa, pdn."
  }
}

variable "region" {
  description = "AWS region of the environment."
  type        = string
  default     = "us-east-1"
}

variable "capacity" {
  description = "Capacity (business domain) that owns the service."
  type        = string
  nullable    = false
}

variable "country" {
  description = "Country code of the service."
  type        = string
  nullable    = false

  validation {
    condition     = can(regex("^[a-z]{2}$", var.country))
    error_message = "country must be a two-letter lowercase code."
  }
}

variable "functionality" {
  description = "Functionality implemented by the service."
  type        = string
  nullable    = false
}

variable "serviceid" {
  description = "Service identifier used for cost allocation."
  type        = string
  nullable    = false
}

variable "bia" {
  description = "Business impact analysis level."
  type        = string
  nullable    = false

  validation {
    condition     = contains(["low", "medium", "high", "critical"], var.bia)
    error_message = "bia must be one of: low, medium, high, critical."
  }
}

variable "confidentiality" {
  description = "Confidentiality classification of the data handled."
  type        = string
  nullable    = false

  validation {
    condition     = contains(["public", "internal", "confidential", "restricted"], var.confidentiality)
    error_message = "confidentiality must be one of: public, internal, confidential, restricted."
  }
}

variable "availability" {
  description = "Availability classification of the service."
  type        = string
  nullable    = false

  validation {
    condition     = contains(["critical", "moderate", "tolerable", "low"], var.availability)
    error_message = "availability must be one of: critical, moderate, tolerable, low."
  }
}

variable "tags" {
  description = "Additional tags merged with the required ones."
  type        = map(string)
  default     = {}
}

variable "container_image_tag" {
  description = "Immutable image tag to deploy (the git commit pushed by scripts/publish-image.sh)."
  type        = string
  nullable    = false
}

variable "cpu" {
  description = "Task CPU units."
  type        = number
  default     = 512
}

variable "memory" {
  description = "Task memory in MiB."
  type        = number
  default     = 1024
}

variable "use_fargate_spot" {
  description = "Run the tasks on Fargate Spot."
  type        = bool
  default     = false
}

variable "desired_count" {
  description = "Initial number of tasks."
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
}

variable "cpu_target_value" {
  description = "Average CPU utilization (%) targeted by auto scaling."
  type        = number
  default     = 60
}

variable "requests_per_target" {
  description = "Requests per task per minute targeted by auto scaling."
  type        = number
  default     = 500
}

variable "log_retention_days" {
  description = "Days the container logs are kept."
  type        = number
  default     = 3
}

variable "listener_port" {
  description = "Port of the load balancer listener the service attaches to: 443 when the transversal stack has a certificate, 80 otherwise."
  type        = number
  default     = 80

  validation {
    condition     = contains([80, 443], var.listener_port)
    error_message = "listener_port must be 80 or 443."
  }
}

variable "listener_rule_priority" {
  description = "Priority of the service routing rule in the load balancer listener."
  type        = number
  default     = 100
}

variable "app_environment" {
  description = "Application settings that differ per environment (log levels, CORS, Swagger, tracing, pool and resilience thresholds)."
  type        = map(string)
  default     = {}
}
