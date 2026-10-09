variable "name" {
  description = "Standard name suffix (<capacity>-<country>-<env>); the load balancer is named alb-ecs-<name>."
  type        = string
  nullable    = false

  validation {
    condition     = length("alb-ecs-${var.name}") <= 32
    error_message = "The load balancer name alb-ecs-<name> must have at most 32 characters."
  }
}

variable "vpc_id" {
  description = "VPC where the load balancer lives."
  type        = string
  nullable    = false
}

variable "vpc_cidr_block" {
  description = "CIDR block of the VPC; the load balancer may only reach targets inside it."
  type        = string
  nullable    = false
}

variable "public_subnet_ids" {
  description = "Public subnets of at least two availability zones."
  type        = list(string)
  nullable    = false

  validation {
    condition     = length(var.public_subnet_ids) >= 2
    error_message = "An application load balancer needs subnets in at least two availability zones."
  }
}

variable "ingress_cidr_blocks" {
  description = "Client CIDR blocks allowed to reach the HTTP listener."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "certificate_arn" {
  description = "ACM certificate for the HTTPS listener. When set, HTTP requests are redirected to HTTPS; when null, the load balancer serves plain HTTP."
  type        = string
  default     = null
}

variable "ssl_policy" {
  description = "TLS negotiation policy of the HTTPS listener."
  type        = string
  default     = "ELBSecurityPolicy-TLS13-1-2-2021-06"
}

variable "deletion_protection" {
  description = "Protect the load balancer from deletion."
  type        = bool
  default     = true
}

variable "idle_timeout" {
  description = "Seconds a connection may stay idle before the load balancer closes it."
  type        = number
  default     = 60
}

variable "tags" {
  description = "Additional tags for the load balancer resources."
  type        = map(string)
  default     = {}
}
