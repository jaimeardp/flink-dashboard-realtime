output "firehose_arn" {
  description = "The ARN of the Kinesis Firehose delivery stream"
  value       = aws_kinesis_firehose_delivery_stream.firehose.arn
}

output "firehose_role_arn" {
  description = "The ARN of the IAM role for Kinesis Firehose"
  value       = aws_iam_role.firehose_role.arn
}
