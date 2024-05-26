# module "iam" {
#   source = "../iam"
# }

resource "aws_kinesis_stream" "example_stream" {
  name             = var.stream_name
  shard_count      = var.shard_count
  retention_period = var.retention_period
}

resource "aws_kinesis_stream" "example_stream_enriched" {
  name             = var.stream_name_enriched
  shard_count      = var.shard_count
  retention_period = var.retention_period
}

resource "aws_s3_bucket" "flink_bucket" {
  bucket = var.s3_bucket_name
}

resource "aws_s3_object" "example" {
  bucket = aws_s3_bucket.flink_bucket.id
  key    = "streaming_pipeline2-1.0-SNAPSHOT.jar"
  source = "~/app-analytics/<>/streaming_pipeline2-1.0-SNAPSHOT.jar"
}

resource "aws_kinesisanalyticsv2_application" "example_app" {
  name        = var.analytics_app_name
  runtime_environment = "FLINK-1_18"
  service_execution_role = var.analytics_role_app_name #module.iam.iam_role_arn

  application_configuration {
    application_code_configuration {
      code_content {
        s3_content_location {
          bucket_arn = aws_s3_bucket.flink_bucket.arn
          file_key   = aws_s3_object.example.key
          object_version = null
        }
      }
      code_content_type = "ZIPFILE"
    }

    environment_properties {
      property_group {
        property_group_id = "FlinkApplicationProperties"
        property_map = {
          "input.stream.name" = aws_kinesis_stream.example_stream.name
        }
      }
    }

    application_snapshot_configuration {
      snapshots_enabled = true
    }

    flink_application_configuration {
      checkpoint_configuration {
        configuration_type = "DEFAULT"
      }

      monitoring_configuration {
        configuration_type = "CUSTOM"
        log_level          = "DEBUG"
        metrics_level      = "TASK"
      }

      parallelism_configuration {
        auto_scaling_enabled = true
        configuration_type   = "CUSTOM"
        parallelism          = 1
        parallelism_per_kpu  = 4
      }
    }

  }
  
  cloudwatch_logging_options {
      log_stream_arn = aws_cloudwatch_log_stream.kda_log_stream.arn
    }
}

resource "aws_cloudwatch_log_group" "kda_log_group" {
  name = var.kda_log_group_name

  tags = {
    Name = var.kda_log_group_name
  }
}

resource "aws_cloudwatch_log_stream" "kda_log_stream" {
  name           = var.kda_log_stream_name
  log_group_name = aws_cloudwatch_log_group.kda_log_group.name
}

