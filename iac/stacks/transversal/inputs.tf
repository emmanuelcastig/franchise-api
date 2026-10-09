variable "env" {
  description = "Environment where the components are deployed."
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
  description = "Capacity (business domain) that owns the components."
  type        = string
  nullable    = false
}

variable "country" {
  description = "Country code of the components."
  type        = string
  nullable    = false

  validation {
    condition     = can(regex("^[a-z]{2}$", var.country))
    error_message = "country must be a two-letter lowercase code."
  }
}

variable "functionality" {
  description = "Functionality of the service that owns the container image repository."
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
  description = "Availability classification of the components."
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

variable "vpc_cidr_block" {
  description = "CIDR block of the environment VPC."
  type        = string
  nullable    = false
}

variable "az_count" {
  description = "Number of availability zones used by the network."
  type        = number
  default     = 2
}

variable "single_nat_gateway" {
  description = "Share one NAT gateway across availability zones."
  type        = bool
  default     = true
}

variable "enable_flow_logs" {
  description = "Capture rejected VPC traffic in CloudWatch Logs."
  type        = bool
  default     = true
}

variable "alb_certificate_arn" {
  description = "ACM certificate for the HTTPS listener; null serves plain HTTP when no domain is available."
  type        = string
  default     = null
}

variable "alb_deletion_protection" {
  description = "Protect the load balancer from deletion."
  type        = bool
  default     = true
}

variable "alb_ingress_cidr_blocks" {
  description = "Client CIDR blocks allowed to reach the load balancer."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "db_instance_class" {
  description = "Instance class of the database."
  type        = string
  default     = "db.t4g.micro"
}

variable "db_allocated_storage" {
  description = "Initial database storage in GiB."
  type        = number
  default     = 20
}

variable "db_max_allocated_storage" {
  description = "Upper limit in GiB for database storage autoscaling."
  type        = number
  default     = 20
}

variable "db_multi_az" {
  description = "Keep a database standby in a second availability zone."
  type        = bool
  default     = false
}

variable "db_backup_retention_days" {
  description = "Days automated database backups are kept."
  type        = number
  default     = 7
}

variable "db_deletion_protection" {
  description = "Protect the database from deletion."
  type        = bool
  default     = true
}

variable "db_skip_final_snapshot" {
  description = "Delete the database without a final snapshot."
  type        = bool
  default     = false
}

variable "db_apply_immediately" {
  description = "Apply database modifications immediately instead of in the maintenance window."
  type        = bool
  default     = false
}

variable "ecr_images_to_keep" {
  description = "Number of images kept in the container repository."
  type        = number
  default     = 5
}

variable "ecr_force_delete" {
  description = "Allow deleting the container repository while it holds images."
  type        = bool
  default     = false
}
