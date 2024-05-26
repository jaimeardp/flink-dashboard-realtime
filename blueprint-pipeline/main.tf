provider "aws" {
  region = var.region
  profile = "streaming_dev"
}

module "iam" {
  source = "./iam"
}

module "kinesis" {
  source = "./kinesis"
  analytics_role_app_name = module.iam.iam_role_arn
  kda_log_group_name  = var.kda_log_group_name
  kda_log_stream_name = var.kda_log_stream_name
}


output "kinesis_stream_arn" {
  value = module.kinesis.kinesis_stream_arn
}

output "kinesis_stream_enriched_arn" {
  value = module.kinesis.kinesis_stream_enriched_arn
}

output "kinesis_analytics_app_arn" {
  value = module.kinesis.kinesis_analytics_app_arn
}


module "firehose" {
  source          = "./firehose"
  bucket_name     = var.bucket_name
  firehose_name   = var.firehose_name
  log_group_name  = var.log_group_name
  log_stream_name = var.log_stream_name
}

output "firehose_arn" {
  value = module.firehose.firehose_arn
}

output "firehose_role_arn" {
  value = module.firehose.firehose_role_arn
}


# Integration with lambda and dynamodb

module "dynamodb" {
  source = "./dynamodb"
  table_name = var.dynamodb_table_name
  read_capacity  = var.dynamodb_read_capacity
  write_capacity = var.dynamodb_write_capacity
}

module "lambda" {
  source            = "./lambda"
  lambda_name       = var.lambda_name
  handler           = var.lambda_handler
  runtime           = var.lambda_runtime
  role_arn          = module.iam.lambda_consumer_iam_role_arn
  kinesis_stream_arn = module.kinesis.kinesis_stream_enriched_arn
}
