provider "aws" {
  alias  = "main"
  region = var.region

  default_tags {
    tags = {
      capacity   = var.capacity
      managed-by = "terraform"
      repository = "franchise-api"
    }
  }
}
