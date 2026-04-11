#!/bin/bash
set -e

yum update -y
yum install -y docker
systemctl enable docker
systemctl start docker

# Add a 2GB swap file to prevent Out-Of-Memory (OOM) kills on t3.micro
fallocate -l 2G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile swap swap defaults 0 0' >> /etc/fstab

# Login to ECR dynamically passed by Terraform
aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${ecr_url}

# Pull Jenkins image from ECR
docker pull ${ecr_url}:latest

# Ensure Jenkins user has permissions for the volume
mkdir -p /var/jenkins_home
chown -R 1000:1000 /var/jenkins_home

# Run Jenkins container
docker run -d --restart=always -p 8080:8080 -p 50000:50000 \
  --name jenkins \
  -v /var/jenkins_home:/var/jenkins_home \
  ${ecr_url}:latest
