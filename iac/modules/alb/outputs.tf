output "arn" {
  description = "ARN of the load balancer."
  value       = aws_lb.this.arn
}

output "arn_suffix" {
  description = "ARN suffix of the load balancer, used by the ALBRequestCountPerTarget scaling metric."
  value       = aws_lb.this.arn_suffix
}

output "dns_name" {
  description = "Public DNS name of the load balancer."
  value       = aws_lb.this.dns_name
}

output "service_listener_arn" {
  description = "Listener where services register their routing rules: HTTPS when a certificate is configured, HTTP otherwise."
  value       = local.https_enabled ? aws_lb_listener.https[0].arn : aws_lb_listener.http.arn
}

output "service_listener_port" {
  description = "Port of the listener where services register their routing rules."
  value       = local.https_enabled ? 443 : 80
}

output "security_group_id" {
  description = "Security group of the load balancer; services allow ingress only from it."
  value       = aws_security_group.alb.id
}
