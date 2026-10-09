module "network" {
  source = "../../modules/network"
  providers = {
    aws.main = aws.main
  }

  name               = local.sufix_standard_name
  cidr_block         = var.vpc_cidr_block
  az_count           = var.az_count
  single_nat_gateway = var.single_nat_gateway
  enable_flow_logs   = var.enable_flow_logs
}

module "ecs_cluster" {
  source = "../../modules/ecs-cluster"
  providers = {
    aws.main = aws.main
  }

  name = local.sufix_standard_name
}

module "alb" {
  source = "../../modules/alb"
  providers = {
    aws.main = aws.main
  }

  name                = local.sufix_standard_name
  vpc_id              = module.network.vpc_id
  vpc_cidr_block      = module.network.vpc_cidr_block
  public_subnet_ids   = module.network.public_subnet_ids
  ingress_cidr_blocks = var.alb_ingress_cidr_blocks
  certificate_arn     = var.alb_certificate_arn
  deletion_protection = var.alb_deletion_protection
}

module "database" {
  source = "../../modules/database"
  providers = {
    aws.main = aws.main
  }

  name                  = local.sufix_standard_name
  vpc_id                = module.network.vpc_id
  subnet_ids            = module.network.private_subnet_ids
  instance_class        = var.db_instance_class
  allocated_storage     = var.db_allocated_storage
  max_allocated_storage = var.db_max_allocated_storage
  multi_az              = var.db_multi_az
  backup_retention_days = var.db_backup_retention_days
  deletion_protection   = var.db_deletion_protection
  skip_final_snapshot   = var.db_skip_final_snapshot
  apply_immediately     = var.db_apply_immediately
}

module "ecr" {
  source = "../../modules/ecr"
  providers = {
    aws.main = aws.main
  }

  name           = local.micro_standard_sufix_name
  images_to_keep = var.ecr_images_to_keep
  force_delete   = var.ecr_force_delete
}
