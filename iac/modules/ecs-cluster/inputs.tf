variable "name" {
  description = "Standard name suffix (<capacity>-<country>-<env>); the cluster is named ecs-<name>."
  type        = string
  nullable    = false
}

variable "container_insights" {
  description = "Enable CloudWatch Container Insights (adds CloudWatch metric costs)."
  type        = bool
  default     = false
}

variable "tags" {
  description = "Additional tags for the cluster."
  type        = map(string)
  default     = {}
}
