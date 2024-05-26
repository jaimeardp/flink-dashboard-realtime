output "iam_role_arn" {
  value = aws_iam_role.kinesis_analytics_role.arn
}

output "lambda_consumer_iam_role_arn" {
  value = aws_iam_role.lambda_consumer_role.arn
}