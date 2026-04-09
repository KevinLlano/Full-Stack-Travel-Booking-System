#!/bin/bash
set -e

yum update -y
yum install -y docker
systemctl enable docker
systemctl start docker

# Login to ECR (replace <aws_account_id> and <region> before use)
# aws ecr get-login-password --region <region> | docker login --username AWS --password-stdin <aws_account_id>.dkr.ecr.<region>.amazonaws.com

# Pull Jenkins image from ECR (replace with your ECR repo URI)
# docker pull <aws_account_id>.dkr.ecr.<region>.amazonaws.com/jenkins:latest

# Run Jenkins container
# Replace image name below with your ECR or Docker Hub image
# For first-time, you can build and test locally, then push to ECR

docker run -d -p 8080:8080 -p 50000:50000 \
  --name jenkins \
  -v /var/jenkins_home:/var/jenkins_home \
  <your-ecr-or-dockerhub-repo>/jenkins:latest

