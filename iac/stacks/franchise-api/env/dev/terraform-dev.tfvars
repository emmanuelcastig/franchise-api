env             = "dev"
region          = "us-east-1"
capacity        = "franchise"
country         = "co"
functionality   = "api"
serviceid       = "franchise-api"
bia             = "low"
confidentiality = "internal"
availability    = "low"
tags            = {}

cpu                    = 512
memory                 = 1024
use_fargate_spot       = true
desired_count          = 1
min_capacity           = 1
max_capacity           = 2
cpu_target_value       = 60
requests_per_target    = 500
log_retention_days     = 3
listener_port          = 80
listener_rule_priority = 100

app_environment = {
  SWAGGER_UI_ENABLED           = "true"
  LOG_LEVEL_ROOT               = "INFO"
  LOG_LEVEL_APP                = "INFO"
  TRACING_SAMPLING_PROBABILITY = "0.1"

  DB_TIMEOUT           = "2s"
  DB_CONNECT_TIMEOUT   = "5s"
  DB_STATEMENT_TIMEOUT = "3s"

  DB_POOL_INITIAL_SIZE     = "1"
  DB_POOL_MAX_SIZE         = "5"
  DB_POOL_MAX_ACQUIRE_TIME = "10s"
  DB_POOL_MAX_IDLE_TIME    = "10m"
  DB_POOL_MAX_LIFE_TIME    = "30m"

  DB_CB_SLIDING_WINDOW_TYPE                = "COUNT_BASED"
  DB_CB_SLIDING_WINDOW_SIZE                = "20"
  DB_CB_MIN_NUMBER_CALLS                   = "10"
  DB_CB_FAILURE_RATE_THRESHOLD             = "50"
  DB_CB_SLOW_CALL_RATE_THRESHOLD           = "80"
  DB_CB_SLOW_CALL_DURATION_THRESHOLD       = "1s"
  DB_CB_WAIT_DURATION_OPEN_STATE           = "15s"
  DB_CB_MAX_WAIT_DURATION_HALF_OPEN_STATE  = "30s"
  DB_CB_PERMITTED_CALLS_HALF_OPEN_STATE    = "3"
  DB_CB_AUTO_TRANS_OPEN_TO_HALF_OPEN_STATE = "true"

  DB_RETRY_MAX_ATTEMPTS                   = "3"
  DB_RETRY_WAIT_DURATION                  = "100ms"
  DB_RETRY_EXPONENTIAL_BACKOFF_ENABLED    = "true"
  DB_RETRY_EXPONENTIAL_BACKOFF_MULTIPLIER = "2"

  SECRETS_CACHE_SECONDS = "300"
}
