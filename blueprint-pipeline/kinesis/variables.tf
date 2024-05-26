variable "stream_name" {
  description = "The name of the Kinesis stream"
  type        = string
  default     = "example-stream"
}

variable "stream_name_enriched" {
  description = "The name of the Kinesis stream with data enriched"
  type        = string
  default     = "example-stream-enriched"
}

variable "retention_period" {
  description = "The number of hours stream records are accessible"
  type        = number
  default     = 24
}

variable "shard_count" {
  description = "The number of shards that the stream uses"
  type        = number
  default     = 1
}

variable "analytics_app_name" {
  description = "The name of the Kinesis Data Analytics application"
  type        = string
  default     = "example-analytics-app"
}

# analytics_role_app_name

variable "analytics_role_app_name" {
  description = "The role of the Kinesis Data Analytics application"
  type        = string
}


variable "s3_bucket_name" {
  description = "The name of the S3 bucket for Flink application"
  type        = string
  default     = "flink-app-bucket-example-jaimeardp"
}

variable "flink_application_jar" {
  description = "The S3 key of the Flink application JAR file"
  type        = string
  default     = "streaming_pipeline2-1.0-SNAPSHOT.jar"
}



variable "kda_log_group_name" {
  description = "The name of the CloudWatch log group"
  type        = string
}

variable "kda_log_stream_name" {
  description = "The name of the CloudWatch log stream"
  type        = string
}
