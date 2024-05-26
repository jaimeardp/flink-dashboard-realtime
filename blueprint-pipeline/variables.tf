variable "region" {
  description = "The AWS region to create resources in"
  type        = string
  default     = "us-east-1"
}

variable "aws_region" {
  description = "The AWS region to deploy resources in"
  type        = string
  default     = "us-east-1"
}

variable "bucket_name" {
  description = "The name of the S3 bucket"
  type        = string
  default     = "my-firehose-bucket-jaimeardp"
}

variable "firehose_name" {
  description = "The name of the Kinesis Firehose delivery stream"
  type        = string
  default     = "my-stream-batch-ingest"
}

variable "log_group_name" {
  description = "The name of the CloudWatch log group"
  type        = string
  default     = "firehose-log-group"
}

variable "log_stream_name" {
  description = "The name of the CloudWatch log stream"
  type        = string
  default     = "firehose-log-stream"
}

variable "kda_log_group_name" {
  description = "The name of the CloudWatch log group"
  type        = string
  default     = "kda-log-group"
}

variable "kda_log_stream_name" {
  description = "The name of the CloudWatch log stream"
  type        = string
  default     = "kda-log-stream"
}


# Integration with lambda and dynamodb


variable "dynamodb_table_name" {
  description = "Name of the DynamoDB table"
  type        = string
  default = "crypto_analysis"
}

variable "dynamodb_read_capacity" {
  description = "Read capacity units for DynamoDB table"
  type        = number
  default = 5
}

variable "dynamodb_write_capacity" {
  description = "Write capacity units for DynamoDB table"
  type        = number
  default = 5
}

# Lambda consumer

variable "lambda_name" {
  description = "Name of the Lambda function"
  type        = string
  default     = "crypto_analysis_lambda"
}

variable "lambda_handler" {
  description = "Lambda handler"
  type        = string
  default     = "lambda_function.lambda_handler"
}

variable "lambda_runtime" {
  description = "Lambda runtime"
  type        = string
  default     = "python3.11"
}

variable "lambda_role_arn" {
  description = "IAM role ARN for the Lambda function"
  type        = string
  default = "value"
}




