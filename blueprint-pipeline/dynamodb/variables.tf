variable "table_name" {
  description = "Name of the DynamoDB table"
  type        = string
}

variable "read_capacity" {
  description = "Read capacity units for DynamoDB table"
  type        = number
}

variable "write_capacity" {
  description = "Write capacity units for DynamoDB table"
  type        = number
}