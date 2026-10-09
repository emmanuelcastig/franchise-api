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

vpc_cidr_block     = "10.40.0.0/16"
az_count           = 3
enable_flow_logs   = true
single_nat_gateway = false

alb_certificate_arn     = null
alb_deletion_protection = true
alb_ingress_cidr_blocks = ["0.0.0.0/0"]

db_instance_class        = "db.t4g.medium"
db_allocated_storage     = 50
db_max_allocated_storage = 200
db_multi_az              = true
db_backup_retention_days = 14
db_deletion_protection   = true
db_apply_immediately     = false
db_skip_final_snapshot   = false

ecr_images_to_keep = 20
ecr_force_delete   = false
