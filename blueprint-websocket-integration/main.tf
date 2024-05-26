resource "aws_iam_role" "lambda_role_websocket" {
  name = "lambda_role_websocket"
  
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Sid    = ""
        Principal = {
          Service = "lambda.amazonaws.com"
        }
     }
    ]
  })

}

resource "aws_iam_policy" "lambda_policy_websocket" {
    name = "lambda_policy_websocket"
    
    policy = jsonencode({
        Version = "2012-10-17"
        Statement = [
        {
            Action = [
            "logs:CreateLogGroup",
            "logs:CreateLogStream",
            "logs:PutLogEvents"
            ]
            Effect   = "Allow"
            Resource = "*"
        },
        {
            Action = [
            "execute-api:ManageConnections"
            ]
            Effect   = "Allow"
            Resource = "*"
        },
        {
            Action = [
            "lambda:InvokeFunction"
            ]
            Effect   = "Allow"
            Resource = "*"
        },
        
        {
            Action = [
            "dynamodb:*"
            ]
            Effect   = "Allow"
            Resource = "*"
        }
        ]
    })
  
}

resource "aws_iam_policy_attachment" "lambda_policy_attachment" {
  name       = "lambda_policy_attachment"
  roles      = [aws_iam_role.lambda_role_websocket.name]
  policy_arn = aws_iam_policy.lambda_policy_websocket.arn
}


data "archive_file" "lambda_websocket_file" {
  type        = "zip"
  source_dir  = "../consumers/lambdas/websocket_managment/"
  output_path = "./zipped_code/bounded_websocket_server.zip"
}

resource "aws_lambda_function" "websocket_lambda" {
  function_name = "websocket_lambda"
  role          = aws_iam_role.lambda_role_websocket.arn
  handler       = "index.handler"
  runtime       = "nodejs16.x"
  filename         = data.archive_file.lambda_websocket_file.output_path
  source_code_hash = data.archive_file.lambda_websocket_file.output_base64sha256
  
  environment {
    variables = {
      foo = "bar"
    }
  }
}

resource "aws_apigatewayv2_api" "websocket_api" {
  name        = "websocket-api"
  protocol_type = "WEBSOCKET"
  route_selection_expression = "$request.body.action"
}

resource "aws_apigatewayv2_route" "connect_route" {
  api_id    = aws_apigatewayv2_api.websocket_api.id
  route_key = "$connect"
  target    = "integrations/${aws_apigatewayv2_integration.websocket_integration.id}"
}

resource "aws_apigatewayv2_route" "message_route" {
  api_id    = aws_apigatewayv2_api.websocket_api.id
  route_key = "$default"
  target    = "integrations/${aws_apigatewayv2_integration.websocket_integration.id}"
}

resource "aws_apigatewayv2_route" "disconnect_route" {
  api_id    = aws_apigatewayv2_api.websocket_api.id
  route_key = "$disconnect"
  target    = "integrations/${aws_apigatewayv2_integration.websocket_integration.id}"
}

resource "aws_apigatewayv2_integration" "websocket_integration" {
  api_id           = aws_apigatewayv2_api.websocket_api.id
  integration_type = "AWS_PROXY"
  integration_uri  = aws_lambda_function.websocket_lambda.invoke_arn
}

resource "aws_apigatewayv2_stage" "websocket_stage" {
  api_id      = aws_apigatewayv2_api.websocket_api.id
  name        = "$default"
  auto_deploy = true
}

resource "aws_lambda_permission" "apigw_lambda" {
  statement_id  = "AllowExecutionFromAPIGateway"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.websocket_lambda.function_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_apigatewayv2_api.websocket_api.execution_arn}/*/*"
}


resource "aws_dynamodb_table" "connections" {
  name           = "websocket_connections"
  hash_key       = "connectionId"
  billing_mode   = "PAY_PER_REQUEST"

  attribute {
    name = "connectionId"
    type = "S"
  }
}
