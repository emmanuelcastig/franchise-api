variable "name" {
  description = "Standard name suffix (<capacity>-<country>-<env>); the instance is named rds-<name>."
  type        = string
  nullable    = false
}

variable "vpc_id" {
  description = "VPC where the database lives."
  type        = string
  nullable    = false
}

variable "subnet_ids" {
  description = "Private subnets of at least two availability zones for the DB subnet group."
  type        = list(string)
  nullable    = false

  validation {
    condition     = length(var.subnet_ids) >= 2
    error_message = "A DB subnet group needs subnets in at least two availability zones."
  }
}

variable "engine_version" {
  description = "PostgreSQL major version; minor versions are upgraded automatically."
  type        = string
  default     = "17"
}

variable "instance_class" {
  description = "Instance class of the database."
  type        = string
  default     = "db.t4g.micro"
}

variable "allocated_storage" {
  description = "Initial storage in GiB."
  type        = number
  default     = 20
}

variable "max_allocated_storage" {
  description = "Upper limit in GiB for storage autoscaling; equal to allocated_storage disables it."
  type        = number
  default     = 20
}

variable "database_name" {
  description = "Name of the database created with the instance."
  type        = string
  default     = "franchise"
}

variable "master_username" {
  description = "Master user name; its password is generated and rotated by RDS in AWS Secrets Manager."
  type        = string
  default     = "franchise_admin"
}

variable "port" {
  description = "Port the database listens on."
  type        = number
  default     = 5432
}

variable "multi_az" {
  description = "Keep a synchronous standby in a second availability zone."
  type        = bool
  default     = false
}

variable "backup_retention_days" {
  description = "Days automated backups are kept."
  type        = number
  default     = 7

  validation {
    condition     = var.backup_retention_days >= 0 && var.backup_retention_days <= 35
    error_message = "backup_retention_days must be between 0 and 35."
  }
}

variable "deletion_protection" {
  description = "Protect the instance from deletion; set it to false only in environments that are destroyed after use."
  type        = bool
  default     = true
}

variable "skip_final_snapshot" {
  description = "Delete the instance without taking a final snapshot."
  type        = bool
  default     = false
}

variable "apply_immediately" {
  description = "Apply modifications immediately instead of during the next maintenance window."
  type        = bool
  default     = false
}

variable "tags" {
  description = "Additional tags for the database resources."
  type        = map(string)
  default     = {}
}
