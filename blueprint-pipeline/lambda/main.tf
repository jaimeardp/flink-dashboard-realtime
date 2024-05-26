
data "archive_file" "lambda" {
  type        = "zip"
  source_dir  = "../consumers/lambdas/pull_data_kinesis_enriched/"
  output_path = "zipped_code/bounded_consumer_lambda.zip"
}

resource "aws_lambda_function" "lambda" {
  function_name = var.lambda_name
  handler       = var.handler
  runtime       = var.runtime
  role          = var.role_arn
  source_code_hash = data.archive_file.lambda.output_base64sha256
  filename         = data.archive_file.lambda.output_path
  timeout = 60
  memory_size = 2048
  environment {
    variables = {
      KINESIS_STREAM_ARN = var.kinesis_stream_arn
    }
  }
}

resource "aws_lambda_event_source_mapping" "kinesis" {
  event_source_arn  = var.kinesis_stream_arn
  function_name     = aws_lambda_function.lambda.function_name
  starting_position = "LATEST"
  batch_size        = 50
}

output "lambda_name" {
  value = aws_lambda_function.lambda.function_name
}
