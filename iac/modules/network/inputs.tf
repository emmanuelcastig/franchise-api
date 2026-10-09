variable "name" {
  description = "Standard name suffix (<capacity>-<country>-<env>) used to name every network resource."
  type        = string
  nullable    = false
}

variable "cidr_block" {
  description = "IPv4 CIDR block of the VPC."
  type        = string
  nullable    = false

  validation {
    condition     = can(cidrnetmask(var.cidr_block))
    error_message = "cidr_block must be a valid IPv4 CIDR block."
  }
}

variable "az_count" {
  description = "Number of availability zones to spread the public and private subnets across."
  type        = number
  default     = 2

  validation {
    condition     = var.az_count >= 2 && var.az_count <= 3
    error_message = "az_count must be 2 or 3; the load balancer and the database subnet group need at least two zones."
  }
}

variable "single_nat_gateway" {
  description = "Share one NAT gateway across all private subnets (cheaper) instead of one per availability zone (highly available)."
  type        = bool
  default     = true
}

variable "enable_flow_logs" {
  description = "Capture rejected VPC traffic in CloudWatch Logs (adds log ingestion costs)."
  type        = bool
  default     = false
}

variable "flow_logs_retention_days" {
  description = "Days the VPC flow logs are kept."
  type        = number
  default     = 30
}

variable "tags" {
  description = "Additional tags for the network resources."
  type        = map(string)
  default     = {}
}
