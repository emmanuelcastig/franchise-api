resource "aws_security_group" "service" {
  provider    = aws.main
  name        = "sh-sg-${var.name}"
  description = "Tasks of ${local.service_name}"
  vpc_id      = var.vpc_id
  tags        = merge(var.tags, { Name = "sh-sg-${var.name}" })
}

resource "aws_vpc_security_group_ingress_rule" "from_load_balancer" {
  provider                     = aws.main
  security_group_id            = aws_security_group.service.id
  referenced_security_group_id = var.alb_security_group_id
  ip_protocol                  = "tcp"
  from_port                    = var.container_port
  to_port                      = var.container_port
  description                  = "Application port from the load balancer only"
}

resource "aws_vpc_security_group_egress_rule" "https" {
  provider          = aws.main
  security_group_id = aws_security_group.service.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "tcp"
  from_port         = 443
  to_port           = 443
  description       = "AWS APIs: Secrets Manager, ECR, CloudWatch Logs"
}

resource "aws_vpc_security_group_egress_rule" "database" {
  provider                     = aws.main
  security_group_id            = aws_security_group.service.id
  referenced_security_group_id = var.database_security_group_id
  ip_protocol                  = "tcp"
  from_port                    = var.database_port
  to_port                      = var.database_port
  description                  = "PostgreSQL"
}
