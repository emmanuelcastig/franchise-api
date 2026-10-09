output "state_bucket_name" {
  description = "Bucket that stores the remote state of every stack and environment."
  value       = aws_s3_bucket.state.bucket
}
