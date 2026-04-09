resource "aws_instance" "jenkins_backend_spot" {
  ami           = "ami-0c94fe40f70e228bc" # public AMI
  instance_type = "t3.micro"

  # Enterprise standard:
  iam_instance_profile = aws_iam_instance_profile.jenkins_profile.name

  instance_market_options {
    market_type = "spot"
    spot_options {

    }
  }

  tags = {
    Name = "jenkins-backend-spot"
  }
  root_block_device {
    volume_size = 6
    volume_type = "gp3"
    delete_on_termination = true
  }

  user_data = file("${path.module}/scripts/run_jenkins_docker.sh")
}

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
