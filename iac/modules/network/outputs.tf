output "vpc_id" {
  description = "Identifier of the VPC."
  value       = aws_vpc.this.id
}

output "vpc_cidr_block" {
  description = "CIDR block of the VPC."
  value       = aws_vpc.this.cidr_block
}

output "public_subnet_ids" {
  description = "Public subnets, used by the internet-facing load balancer and the NAT gateways."
  value       = aws_subnet.public[*].id
}

output "private_subnet_ids" {
  description = "Private subnets, used by the ECS tasks and the database."
  value       = aws_subnet.private[*].id
}
