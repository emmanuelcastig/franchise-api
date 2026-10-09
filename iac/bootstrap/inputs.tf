variable "region" {
  description = "Region of the state bucket."
  type        = string
  default     = "us-east-1"
}

variable "capacity" {
  description = "Capacity (business domain) that owns the state bucket."
  type        = string
  default     = "franchise"
}

variable "noncurrent_version_retention_days" {
  description = "Days previous versions of a state file are kept before they expire."
  type        = number
  default     = 30
}

variable "force_destroy" {
  description = "Allow destroying the bucket while it still holds state files."
  type        = bool
  default     = false
}
