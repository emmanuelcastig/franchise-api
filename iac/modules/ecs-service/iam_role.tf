data "aws_iam_policy_document" "ecs_tasks_trust" {
  provider = aws.main

  statement {
    sid     = "EcsTasksAssumeRole"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }

    condition {
      test     = "StringEquals"
      variable = "aws:SourceAccount"
      values   = [data.aws_caller_identity.current.account_id]
    }
  }
}

resource "aws_iam_role" "execution" {
  provider             = aws.main
  name                 = local.execution_role_name
  assume_role_policy   = data.aws_iam_policy_document.ecs_tasks_trust.json
  permissions_boundary = var.permissions_boundary_arn
  max_session_duration = 3600
  tags                 = var.tags
}

data "aws_iam_policy_document" "execution" {
  provider = aws.main

  statement {
    sid       = "CloudWatchLogs"
    actions   = ["logs:CreateLogStream", "logs:PutLogEvents"]
    resources = ["${aws_cloudwatch_log_group.this.arn}:*"]
  }

  statement {
    sid       = "EcrPullImage"
    actions   = ["ecr:BatchCheckLayerAvailability", "ecr:BatchGetImage", "ecr:GetDownloadUrlForLayer"]
    resources = [var.ecr_repository_arn]
  }

  statement {
    sid       = "EcrAuthorization"
    actions   = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }
}

resource "aws_iam_role_policy" "execution" {
  provider = aws.main
  name     = "${local.execution_role_name}-policy"
  role     = aws_iam_role.execution.id
  policy   = data.aws_iam_policy_document.execution.json
}

resource "aws_iam_role" "task" {
  provider             = aws.main
  name                 = local.task_role_name
  assume_role_policy   = data.aws_iam_policy_document.ecs_tasks_trust.json
  permissions_boundary = var.permissions_boundary_arn
  max_session_duration = 3600
  tags                 = var.tags
}

data "aws_iam_policy_document" "task" {
  provider = aws.main

  statement {
    sid       = "ReadDatabaseCredentials"
    actions   = ["secretsmanager:GetSecretValue", "secretsmanager:DescribeSecret"]
    resources = var.secret_arns

    condition {
      test     = "StringEquals"
      variable = "aws:RequestedRegion"
      values   = [data.aws_region.current.region]
    }
  }
}

resource "aws_iam_role_policy" "task" {
  provider = aws.main
  name     = "${local.task_role_name}-policy"
  role     = aws_iam_role.task.id
  policy   = data.aws_iam_policy_document.task.json
}
