provider "aws" {
  alias  = "main"
  region = var.region

  default_tags {
    tags = local.tags
  }
}
