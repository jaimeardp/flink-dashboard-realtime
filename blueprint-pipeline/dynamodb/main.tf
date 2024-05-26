resource "aws_dynamodb_table" "table" {
  name           = var.table_name
  read_capacity  = var.read_capacity
  write_capacity = var.write_capacity
  hash_key       = "hashkey_id"

  attribute {
    name = "hashkey_id"
    type = "S"
  }

  tags = {
    Name = var.table_name
  }
}

output "table_name" {
  value = aws_dynamodb_table.table.name
}
