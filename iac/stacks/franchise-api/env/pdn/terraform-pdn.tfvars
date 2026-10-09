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
  LOG_LEVEL_ROOT               = "WARN"
  LOG_LEVEL_APP                = "INFO"
  TRACING_SAMPLING_PROBABILITY = "0.05"

  DB_TIMEOUT           = "1500ms"
  DB_CONNECT_TIMEOUT   = "3s"
  DB_STATEMENT_TIMEOUT = "2s"

  DB_POOL_INITIAL_SIZE     = "5"
  DB_POOL_MAX_SIZE         = "20"
  DB_POOL_MAX_ACQUIRE_TIME = "10s"
  DB_POOL_MAX_IDLE_TIME    = "5m"
  DB_POOL_MAX_LIFE_TIME    = "30m"

  DB_CB_SLIDING_WINDOW_TYPE                = "TIME_BASED"
  DB_CB_SLIDING_WINDOW_SIZE                = "60"
  DB_CB_MIN_NUMBER_CALLS                   = "50"
  DB_CB_FAILURE_RATE_THRESHOLD             = "40"
  DB_CB_SLOW_CALL_RATE_THRESHOLD           = "60"
  DB_CB_SLOW_CALL_DURATION_THRESHOLD       = "800ms"
  DB_CB_WAIT_DURATION_OPEN_STATE           = "30s"
  DB_CB_MAX_WAIT_DURATION_HALF_OPEN_STATE  = "60s"
  DB_CB_PERMITTED_CALLS_HALF_OPEN_STATE    = "10"
  DB_CB_AUTO_TRANS_OPEN_TO_HALF_OPEN_STATE = "true"

  DB_RETRY_MAX_ATTEMPTS                   = "3"
  DB_RETRY_WAIT_DURATION                  = "200ms"
  DB_RETRY_EXPONENTIAL_BACKOFF_ENABLED    = "true"
  DB_RETRY_EXPONENTIAL_BACKOFF_MULTIPLIER = "2"

  SECRETS_CACHE_SECONDS = "600"
}
