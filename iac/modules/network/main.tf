data "aws_availability_zones" "available" {
  provider = aws.main
  state    = "available"
}

data "aws_region" "current" {
  provider = aws.main
}

locals {
  availability_zones = slice(data.aws_availability_zones.available.names, 0, var.az_count)
  nat_gateway_count  = var.single_nat_gateway ? 1 : var.az_count
}

resource "aws_vpc" "this" {
  provider             = aws.main
  cidr_block           = var.cidr_block
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags                 = merge(var.tags, { Name = "vpc-${var.name}" })
}

resource "aws_default_security_group" "this" {
  provider = aws.main
  vpc_id   = aws_vpc.this.id
  tags     = merge(var.tags, { Name = "sg-default-${var.name}" })
}

resource "aws_internet_gateway" "this" {
  provider = aws.main
  vpc_id   = aws_vpc.this.id
  tags     = merge(var.tags, { Name = "igw-${var.name}" })
}

resource "aws_subnet" "public" {
  provider          = aws.main
  count             = var.az_count
  vpc_id            = aws_vpc.this.id
  cidr_block        = cidrsubnet(var.cidr_block, 8, count.index)
  availability_zone = local.availability_zones[count.index]
  tags              = merge(var.tags, { Name = "subnet-public-${var.name}-${count.index + 1}", tier = "public" })
}

resource "aws_subnet" "private" {
  provider          = aws.main
  count             = var.az_count
  vpc_id            = aws_vpc.this.id
  cidr_block        = cidrsubnet(var.cidr_block, 8, count.index + 10)
  availability_zone = local.availability_zones[count.index]
  tags              = merge(var.tags, { Name = "subnet-private-${var.name}-${count.index + 1}", tier = "private" })
}

resource "aws_eip" "nat" {
  provider = aws.main
  count    = local.nat_gateway_count
  domain   = "vpc"
  tags     = merge(var.tags, { Name = "eip-nat-${var.name}-${count.index + 1}" })
}

resource "aws_nat_gateway" "this" {
  provider      = aws.main
  count         = local.nat_gateway_count
  allocation_id = aws_eip.nat[count.index].id
  subnet_id     = aws_subnet.public[count.index].id
  tags          = merge(var.tags, { Name = "nat-${var.name}-${count.index + 1}" })

  depends_on = [aws_internet_gateway.this]
}

resource "aws_route_table" "public" {
  provider = aws.main
  vpc_id   = aws_vpc.this.id
  tags     = merge(var.tags, { Name = "rt-public-${var.name}" })

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.this.id
  }
}

resource "aws_route_table_association" "public" {
  provider       = aws.main
  count          = var.az_count
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table" "private" {
  provider = aws.main
  count    = local.nat_gateway_count
  vpc_id   = aws_vpc.this.id
  tags     = merge(var.tags, { Name = "rt-private-${var.name}-${count.index + 1}" })

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.this[count.index].id
  }
}

resource "aws_route_table_association" "private" {
  provider       = aws.main
  count          = var.az_count
  subnet_id      = aws_subnet.private[count.index].id
  route_table_id = aws_route_table.private[var.single_nat_gateway ? 0 : count.index].id
}

resource "aws_vpc_endpoint" "s3" {
  provider          = aws.main
  vpc_id            = aws_vpc.this.id
  service_name      = "com.amazonaws.${data.aws_region.current.region}.s3"
  vpc_endpoint_type = "Gateway"
  route_table_ids   = aws_route_table.private[*].id
  tags              = merge(var.tags, { Name = "vpce-s3-${var.name}" })
}
