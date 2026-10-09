locals {
  sufix_standard_name       = "${var.capacity}-${var.country}-${var.env}"
  micro_standard_sufix_name = "${var.capacity}-${var.country}-${var.functionality}-${var.env}"
}

locals {
  required_tags = {
    capacity        = var.capacity
    country         = var.country
    env             = var.env
    serviceid       = var.serviceid
    bia             = var.bia
    confidentiality = var.confidentiality
    availability    = var.availability
    managed-by      = "terraform"
    repository      = "franchise-api"
  }

  tags = merge(var.tags, local.required_tags)
}
