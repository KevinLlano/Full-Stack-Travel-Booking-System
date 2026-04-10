# Get the latest Amazon Linux 2023 AMI (enterprise best practice)
data "aws_ami" "latest_amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

resource "aws_instance" "jenkins_backend_spot" {
  ami           = data.aws_ami.latest_amazon_linux_2023.id
  instance_type = "t3.micro"

  # Enterprise standard:
  iam_instance_profile   = aws_iam_instance_profile.jenkins_profile.name
  vpc_security_group_ids = [aws_security_group.jenkins_sg.id]

  instance_market_options {
    market_type = "spot"
    spot_options {

    }
  }

  tags = {
    Name = "jenkins-backend-spot"
  }
  root_block_device {
    volume_size = 8
    volume_type = "gp3"
    delete_on_termination = true
  }

  user_data = templatefile("${path.module}/scripts/run_jenkins_docker.sh", {
    region  = data.aws_region.current.id,
    ecr_url = aws_ecr_repository.jenkins.repository_url
  })
}

resource "aws_security_group" "jenkins_sg" {
  name        = "jenkins_sg"
  description = "Security group for Jenkins EC2"

  ingress {
    description = "Allow Jenkins Web UI"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # Restrict to a specific VPN/office IP CIDR instead of 0.0.0.0/0 in production!
  }

  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

data "aws_region" "current" {}

resource "aws_ecr_repository" "jenkins" {
  name                 = "jenkins"
  image_tag_mutability = "MUTABLE"
  force_delete         = true
}

output "jenkins_ecr_repo_url" {
  value = aws_ecr_repository.jenkins.repository_url
}

# Attach ECR read-only policy to Jenkins role
# (Assumes aws_iam_role.jenkins_role is defined in iam.tf)
data "aws_iam_policy" "ecr_readonly" {
  arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

resource "aws_iam_role_policy_attachment" "jenkins_ecr_readonly" {
  role       = aws_iam_role.jenkins_role.name
  policy_arn = data.aws_iam_policy.ecr_readonly.arn
}
