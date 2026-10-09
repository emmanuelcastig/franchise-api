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

locals {
  api_url             = "${var.listener_port == 443 ? "https" : "http"}://${data.aws_lb.selected.dns_name}"
  database_secret_arn = data.aws_db_instance.selected.master_user_secret[0].secret_arn

  container_environment = merge(var.app_environment, {
    SPRING_PROFILES_ACTIVE = var.env
    AWS_REGION             = data.aws_region.current.region
    DB_HOST                = data.aws_db_instance.selected.address
    DB_PORT                = tostring(data.aws_db_instance.selected.port)
    DB_NAME                = data.aws_db_instance.selected.db_name
    DB_SSL_MODE            = "require"
    DB_SECRET_NAME         = local.database_secret_arn
  })
}
