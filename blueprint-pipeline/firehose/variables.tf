variable "bucket_name" {
  description = "The name of the S3 bucket"
  type        = string
}

variable "firehose_name" {
  description = "The name of the Kinesis Firehose delivery stream"
  type        = string
}

variable "log_group_name" {
  description = "The name of the CloudWatch log group"
  type        = string
}

variable "log_stream_name" {
  description = "The name of the CloudWatch log stream"
  type        = string
}
