resource "aws_s3_bucket" "firehose_bucket" {
  bucket = var.bucket_name

  tags = {
    Name = var.bucket_name
  }
}

resource "aws_iam_role" "firehose_role" {
  name = "${var.firehose_name}_role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          Service = "firehose.amazonaws.com"
        },
        Action = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_policy" "firehose_policy" {
  name        = "${var.firehose_name}_policy"
  description = "Policy for Kinesis Firehose delivery to S3"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "s3:AbortMultipartUpload",
          "s3:GetBucketLocation",
          "s3:GetObject",
          "s3:ListBucket",
          "s3:ListBucketMultipartUploads",
          "s3:PutObject"
        ],
        Resource = [
          aws_s3_bucket.firehose_bucket.arn,
          "${aws_s3_bucket.firehose_bucket.arn}/*"
        ]
      },
      {
        Effect = "Allow",
        Action = [
          "kms:Decrypt",
          "kms:GenerateDataKey"
        ],
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "firehose_policy_attachment" {
  role       = aws_iam_role.firehose_role.name
  policy_arn = aws_iam_policy.firehose_policy.arn
}

resource "aws_kinesis_firehose_delivery_stream" "firehose" {
  name        = var.firehose_name
  destination = "extended_s3"

  extended_s3_configuration {
    role_arn           = aws_iam_role.firehose_role.arn
    bucket_arn         = aws_s3_bucket.firehose_bucket.arn
    buffering_size = 64
    buffering_interval = 150
    compression_format = "UNCOMPRESSED"

    cloudwatch_logging_options {
      enabled         = true
      log_group_name  = var.log_group_name
      log_stream_name = var.log_stream_name
    }

    processing_configuration {
      enabled = false
    }
  }

  tags = {
    Name = var.firehose_name
  }
}

resource "aws_cloudwatch_log_group" "firehose_log_group" {
  name = var.log_group_name

  tags = {
    Name = var.log_group_name
  }
}

resource "aws_cloudwatch_log_stream" "firehose_log_stream" {
  name           = var.log_stream_name
  log_group_name = aws_cloudwatch_log_group.firehose_log_group.name
}
