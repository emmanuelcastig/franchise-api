data "aws_caller_identity" "current" {
  provider = aws.main
}

resource "aws_cloudwatch_log_group" "flow_logs" {
  provider          = aws.main
  count             = var.enable_flow_logs ? 1 : 0
  name              = "/aws/vpc/flow-logs/vpc-${var.name}"
  retention_in_days = var.flow_logs_retention_days
  tags              = var.tags
}

data "aws_iam_policy_document" "flow_logs_trust" {
  provider = aws.main

  statement {
    sid     = "VpcFlowLogsAssumeRole"
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["vpc-flow-logs.amazonaws.com"]
    }

    condition {
      test     = "StringEquals"
      variable = "aws:SourceAccount"
      values   = [data.aws_caller_identity.current.account_id]
    }
  }
}

resource "aws_iam_role" "flow_logs" {
  provider           = aws.main
  count              = var.enable_flow_logs ? 1 : 0
  name               = "vpc-${var.name}-flow-logs-role"
  assume_role_policy = data.aws_iam_policy_document.flow_logs_trust.json
  tags               = var.tags
}

data "aws_iam_policy_document" "flow_logs" {
  provider = aws.main
  count    = var.enable_flow_logs ? 1 : 0

  statement {
    sid       = "WriteFlowLogs"
    actions   = ["logs:CreateLogStream", "logs:PutLogEvents", "logs:DescribeLogStreams"]
    resources = ["${aws_cloudwatch_log_group.flow_logs[0].arn}:*"]
  }
}

resource "aws_iam_role_policy" "flow_logs" {
  provider = aws.main
  count    = var.enable_flow_logs ? 1 : 0
  name     = "vpc-${var.name}-flow-logs-policy"
  role     = aws_iam_role.flow_logs[0].id
  policy   = data.aws_iam_policy_document.flow_logs[0].json
}

resource "aws_flow_log" "this" {
  provider        = aws.main
  count           = var.enable_flow_logs ? 1 : 0
  vpc_id          = aws_vpc.this.id
  traffic_type    = "REJECT"
  log_destination = aws_cloudwatch_log_group.flow_logs[0].arn
  iam_role_arn    = aws_iam_role.flow_logs[0].arn
  tags            = merge(var.tags, { Name = "flow-logs-vpc-${var.name}" })
}
