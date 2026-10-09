env             = "qa"
region          = "us-east-1"
capacity        = "franchise"
country         = "co"
functionality   = "api"
serviceid       = "franchise-api"
bia             = "medium"
confidentiality = "internal"
availability    = "tolerable"
tags            = {}

cpu                    = 512
memory                 = 1024
use_fargate_spot       = true
desired_count          = 2
min_capacity           = 2
max_capacity           = 4
cpu_target_value       = 60
requests_per_target    = 500
log_retention_days     = 7
listener_port          = 80
listener_rule_priority = 100

app_environment = {
  SWAGGER_UI_ENABLED           = "true"
  LOG_LEVEL_APP                = "INFO"
  TRACING_SAMPLING_PROBABILITY = "0.2"
  DB_POOL_MAX_SIZE             = "10"
}
