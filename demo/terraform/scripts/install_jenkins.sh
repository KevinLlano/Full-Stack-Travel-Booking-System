#!/bin/bash
# Jenkins install script for Amazon Linux 2 (modify for Ubuntu if needed)
set -e

yum update -y
yum install -y java-17-openjdk wget

# Add Jenkins repo and import key
wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io-2023.key

yum install -y jenkins
systemctl enable jenkins
systemctl start jenkins

# Optionally install plugins via CLI or UI after Jenkins is up
# Example: /usr/bin/jenkins-cli.jar -s http://localhost:8080/ install-plugin trivy sonar

