resource "aws_security_group" "database" {
  provider    = aws.main
  name        = "sh-sg-rds-${var.name}"
  description = "PostgreSQL of ${var.name}; ingress rules are added by the services that use it"
  vpc_id      = var.vpc_id
  tags        = merge(var.tags, { Name = "sh-sg-rds-${var.name}" })
}

resource "aws_db_subnet_group" "this" {
  provider   = aws.main
  name       = "rds-${var.name}"
  subnet_ids = var.subnet_ids
  tags       = var.tags
}

resource "aws_db_instance" "this" {
  provider                     = aws.main
  identifier                   = "rds-${var.name}"
  engine                       = "postgres"
  engine_version               = var.engine_version
  instance_class               = var.instance_class
  allocated_storage            = var.allocated_storage
  max_allocated_storage        = var.max_allocated_storage
  storage_type                 = "gp3"
  storage_encrypted            = true
  db_name                      = var.database_name
  username                     = var.master_username
  manage_master_user_password  = true
  port                         = var.port
  db_subnet_group_name         = aws_db_subnet_group.this.name
  vpc_security_group_ids       = [aws_security_group.database.id]
  publicly_accessible          = false
  multi_az                     = var.multi_az
  backup_retention_period      = var.backup_retention_days
  deletion_protection          = var.deletion_protection
  skip_final_snapshot          = var.skip_final_snapshot
  final_snapshot_identifier    = var.skip_final_snapshot ? null : "rds-${var.name}-final"
  auto_minor_version_upgrade   = true
  allow_major_version_upgrade  = false
  apply_immediately            = var.apply_immediately
  copy_tags_to_snapshot        = true
  performance_insights_enabled = false
  tags                         = var.tags
}
