module "ecs_service" {
  source = "../../modules/ecs-service"
  providers = {
    aws.main = aws.main
  }

  name                       = local.micro_standard_sufix_name
  cluster_name               = data.aws_ecs_cluster.selected.cluster_name
  vpc_id                     = data.aws_vpc.selected.id
  subnet_ids                 = data.aws_subnets.private.ids
  listener_arn               = data.aws_lb_listener.service.arn
  listener_rule_priority     = var.listener_rule_priority
  alb_security_group_id      = data.aws_security_group.alb.id
  alb_arn_suffix             = data.aws_lb.selected.arn_suffix
  container_image            = "${data.aws_ecr_repository.selected.repository_url}:${var.container_image_tag}"
  ecr_repository_arn         = data.aws_ecr_repository.selected.arn
  cpu                        = var.cpu
  memory                     = var.memory
  environment                = local.container_environment
  secret_arns                = [local.database_secret_arn]
  database_security_group_id = data.aws_security_group.database.id
  database_port              = data.aws_db_instance.selected.port
  use_fargate_spot           = var.use_fargate_spot
  desired_count              = var.desired_count
  min_capacity               = var.min_capacity
  max_capacity               = var.max_capacity
  cpu_target_value           = var.cpu_target_value
  requests_per_target        = var.requests_per_target
  log_retention_days         = var.log_retention_days
}

resource "aws_vpc_security_group_ingress_rule" "database_from_service" {
  provider                     = aws.main
  security_group_id            = data.aws_security_group.database.id
  referenced_security_group_id = module.ecs_service.security_group_id
  ip_protocol                  = "tcp"
  from_port                    = data.aws_db_instance.selected.port
  to_port                      = data.aws_db_instance.selected.port
  description                  = "PostgreSQL from ${module.ecs_service.service_name}"
}
