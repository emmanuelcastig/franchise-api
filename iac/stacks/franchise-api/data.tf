data "aws_region" "current" {
  provider = aws.main
}

data "aws_vpc" "selected" {
  provider = aws.main

  filter {
    name   = "tag:Name"
    values = ["vpc-${local.sufix_standard_name}"]
  }
}

data "aws_subnets" "private" {
  provider = aws.main

  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.selected.id]
  }

  filter {
    name   = "tag:tier"
    values = ["private"]
  }
}

data "aws_ecs_cluster" "selected" {
  provider     = aws.main
  cluster_name = "ecs-${local.sufix_standard_name}"
}

data "aws_lb" "selected" {
  provider = aws.main
  name     = "alb-ecs-${local.sufix_standard_name}"
}

data "aws_lb_listener" "service" {
  provider          = aws.main
  load_balancer_arn = data.aws_lb.selected.arn
  port              = var.listener_port
}

data "aws_security_group" "alb" {
  provider = aws.main
  name     = "sh-sg-alb-${local.sufix_standard_name}"
  vpc_id   = data.aws_vpc.selected.id
}

data "aws_db_instance" "selected" {
  provider               = aws.main
  db_instance_identifier = "rds-${local.sufix_standard_name}"
}

data "aws_security_group" "database" {
  provider = aws.main
  name     = "sh-sg-rds-${local.sufix_standard_name}"
  vpc_id   = data.aws_vpc.selected.id
}

data "aws_ecr_repository" "selected" {
  provider = aws.main
  name     = local.micro_standard_sufix_name
}
