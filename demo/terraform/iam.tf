data "aws_iam_policy_document" "jenkins_assume_role_policy" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "jenkins_role" {
  name = "jenkins-backend-role"
  assume_role_policy = data.aws_iam_policy_document.jenkins_assume_role_policy.json
}

resource "aws_iam_instance_profile" "jenkins_profile" {
  name = "jenkins-backend-profile"
  role = aws_iam_role.jenkins_role.name
}

# Attach SSM policy for Session Manager access (enterprise best practice)
resource "aws_iam_role_policy_attachment" "jenkins_ssm" {
  role       = aws_iam_role.jenkins_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

