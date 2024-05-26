output "kinesis_stream_arn" {
  value = aws_kinesis_stream.example_stream.arn
}

output "kinesis_stream_enriched_arn" {
  value = aws_kinesis_stream.example_stream_enriched.arn
}

output "kinesis_analytics_app_arn" {
  value = aws_kinesisanalyticsv2_application.example_app.arn
}

output "s3_bucket_arn" {
  value = aws_s3_bucket.flink_bucket.arn
}
