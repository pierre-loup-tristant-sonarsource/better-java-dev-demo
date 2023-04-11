terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "3.5.0"
    }
  }
}

provider "aws" {
  profile = "default"
  region  = var.region
  access_key = "AKIA33KVYFLTLW7ERL37" # var.aws_access_key
  secret_key = "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" # var.aws_secret_key
}

resource "aws_s3_bucket" "app_bucket" {
  bucket = "sonar.devoxx2023.demo"
}

resource "aws_s3_bucket_public_access_block" "example-public-access-block" {
  bucket = aws_s3_bucket.app_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
