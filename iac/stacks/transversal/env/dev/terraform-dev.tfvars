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

vpc_cidr_block     = "10.20.0.0/16"
az_count           = 2
enable_flow_logs   = false
single_nat_gateway = true

alb_certificate_arn     = null
alb_deletion_protection = false
alb_ingress_cidr_blocks = ["0.0.0.0/0"]

db_instance_class        = "db.t4g.micro"
db_allocated_storage     = 20
db_max_allocated_storage = 20
db_multi_az              = false
db_backup_retention_days = 1
db_deletion_protection   = false
db_apply_immediately     = true
db_skip_final_snapshot   = true

ecr_images_to_keep = 5
ecr_force_delete   = true
