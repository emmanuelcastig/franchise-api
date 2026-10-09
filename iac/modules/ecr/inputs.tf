variable "name" {
  description = "Repository name (<capacity>-<country>-<functionality>-<env>)."
  type        = string
  nullable    = false
}

variable "images_to_keep" {
  description = "Number of most recent images kept; older ones are expired by the lifecycle policy."
  type        = number
  default     = 5
}

variable "force_delete" {
  description = "Allow deleting the repository while it still contains images."
  type        = bool
  default     = false
}

variable "tags" {
  description = "Additional tags for the repository."
  type        = map(string)
  default     = {}
}
