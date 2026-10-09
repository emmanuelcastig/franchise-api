data "aws_region" "current" {
  provider = aws.main
}

data "aws_caller_identity" "current" {
  provider = aws.main
}

locals {
  service_name        = "svc-${var.name}"
  task_family         = "task-${var.name}"
  container_name      = var.name
  log_group_name      = "/aws/ecs/svc-${var.name}"
  target_group_name   = "tg-${var.name}"
  task_role_name      = "${var.name}-task-role"
  execution_role_name = "${var.name}-ex-role"
  capacity_provider   = var.use_fargate_spot ? "FARGATE_SPOT" : "FARGATE"
}

locals {
  container_environment = [for name, value in var.environment : { name = name, value = value }]
}
