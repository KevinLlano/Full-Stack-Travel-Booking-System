
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
}
