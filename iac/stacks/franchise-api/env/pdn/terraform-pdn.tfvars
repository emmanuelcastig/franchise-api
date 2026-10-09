env             = "pdn"
region          = "us-east-1"
capacity        = "franchise"
country         = "co"
functionality   = "api"
serviceid       = "franchise-api"
bia             = "high"
confidentiality = "internal"
availability    = "critical"
tags            = {}

cpu                    = 1024
memory                 = 2048
use_fargate_spot       = false
desired_count          = 3
min_capacity           = 3
max_capacity           = 10
cpu_target_value       = 60
requests_per_target    = 1000
log_retention_days     = 30
listener_port          = 80
listener_rule_priority = 100

app_environment = {
  SWAGGER_UI_ENABLED           = "false"
  LOG_LEVEL_APP                = "INFO"
  TRACING_SAMPLING_PROBABILITY = "0.05"
  DB_POOL_MAX_SIZE             = "20"
}
