#!/bin/bash
set -e

yum update -y
yum install -y docker
systemctl enable docker
systemctl start docker

# Login to ECR dynamically passed by Terraform
aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${ecr_url}

# Pull Jenkins image from ECR
docker pull ${ecr_url}:latest

# Ensure Jenkins user has permissions for the volume
mkdir -p /var/jenkins_home
chown -R 1000:1000 /var/jenkins_home

# Run Jenkins container
docker run -d -p 8080:8080 -p 50000:50000 \
  --name jenkins \
  -v /var/jenkins_home:/var/jenkins_home \
  ${ecr_url}:latest
