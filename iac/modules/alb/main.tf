locals {
  https_enabled  = var.certificate_arn != null
  not_found_body = jsonencode({ status = 404, title = "Not Found" })
}

resource "aws_security_group" "alb" {
  provider    = aws.main
  name        = "sh-sg-alb-${var.name}"
  description = "Internet-facing load balancer of ${var.name}"
  vpc_id      = var.vpc_id
  tags        = merge(var.tags, { Name = "sh-sg-alb-${var.name}" })
}

resource "aws_vpc_security_group_ingress_rule" "http" {
  provider          = aws.main
  for_each          = toset(var.ingress_cidr_blocks)
  security_group_id = aws_security_group.alb.id
  cidr_ipv4         = each.value
  ip_protocol       = "tcp"
  from_port         = 80
  to_port           = 80
  description       = "HTTP from clients"
}

resource "aws_vpc_security_group_ingress_rule" "https" {
  provider          = aws.main
  for_each          = local.https_enabled ? toset(var.ingress_cidr_blocks) : toset([])
  security_group_id = aws_security_group.alb.id
  cidr_ipv4         = each.value
  ip_protocol       = "tcp"
  from_port         = 443
  to_port           = 443
  description       = "HTTPS from clients"
}

resource "aws_vpc_security_group_egress_rule" "targets" {
  provider          = aws.main
  security_group_id = aws_security_group.alb.id
  cidr_ipv4         = var.vpc_cidr_block
  ip_protocol       = "tcp"
  from_port         = 0
  to_port           = 65535
  description       = "Targets inside the VPC"
}

resource "aws_lb" "this" {
  provider                   = aws.main
  name                       = "alb-ecs-${var.name}"
  internal                   = false
  load_balancer_type         = "application"
  security_groups            = [aws_security_group.alb.id]
  subnets                    = var.public_subnet_ids
  idle_timeout               = var.idle_timeout
  drop_invalid_header_fields = true
  enable_deletion_protection = var.deletion_protection
  tags                       = var.tags
}

resource "aws_lb_listener" "http" {
  provider          = aws.main
  load_balancer_arn = aws_lb.this.arn
  port              = 80
  protocol          = "HTTP"
  tags              = var.tags

  default_action {
    type = local.https_enabled ? "redirect" : "fixed-response"

    dynamic "redirect" {
      for_each = local.https_enabled ? [1] : []

      content {
        port        = "443"
        protocol    = "HTTPS"
        status_code = "HTTP_301"
      }
    }

    dynamic "fixed_response" {
      for_each = local.https_enabled ? [] : [1]

      content {
        content_type = "application/json"
        message_body = local.not_found_body
        status_code  = "404"
      }
    }
  }
}

resource "aws_lb_listener" "https" {
  provider          = aws.main
  count             = local.https_enabled ? 1 : 0
  load_balancer_arn = aws_lb.this.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = var.ssl_policy
  certificate_arn   = var.certificate_arn
  tags              = var.tags

  default_action {
    type = "fixed-response"

    fixed_response {
      content_type = "application/json"
      message_body = local.not_found_body
      status_code  = "404"
    }
  }
}
