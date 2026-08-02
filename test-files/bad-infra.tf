# Terraform config — EU client data stored in US region (VIOLATION)
resource "aws_s3_bucket" "eu_client_data" {
  bucket = "eurolend-loan-documents"
  region = "us-east-1"  # VIOLATION: EU/GDPR client data must stay in eu-central-1
  
  tags = {
    client = "EuroLend Financial Group"
    data_classification = "PII"
    gdpr = "true"
  }
}

resource "aws_s3_bucket_public_access_block" "public" {
  bucket = aws_s3_bucket.eu_client_data.id
  block_public_acls = false  # VIOLATION: public access enabled
}
