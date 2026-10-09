#!/usr/bin/env bash
set -euo pipefail

IAC_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REPO_DIR="$(cd "${IAC_DIR}/.." && pwd)"
TF="${TERRAFORM:-terraform}"
AWS_REGION="${AWS_REGION:-us-east-1}"
CAPACITY="franchise"
COUNTRY="co"
FUNCTIONALITY="api"

APPROVE=()
if [[ "${AUTO_APPROVE:-false}" == "true" ]]; then
  APPROVE=(-auto-approve)
fi

require_env() {
  local env="${1:-}"
  if [[ ! "${env}" =~ ^(dev|qa|pdn)$ ]]; then
    echo "usage: $(basename "$0") <dev|qa|pdn>" >&2
    exit 1
  fi
}

account_id() {
  aws sts get-caller-identity --query Account --output text
}

state_bucket() {
  echo "tfstate-${CAPACITY}-$(account_id)-${AWS_REGION}"
}

stack_init() {
  local stack="$1" env="$2"
  "${TF}" -chdir="${IAC_DIR}/stacks/${stack}" init -input=false -reconfigure \
    -backend-config="env/${env}/backend.hcl" \
    -backend-config="bucket=$(state_bucket)"
}

stack_apply() {
  local stack="$1" env="$2"
  shift 2
  stack_init "${stack}" "${env}"
  "${TF}" -chdir="${IAC_DIR}/stacks/${stack}" apply -input=false \
    -var-file="env/${env}/terraform-${env}.tfvars" "$@" ${APPROVE[@]+"${APPROVE[@]}"}
}

stack_destroy() {
  local stack="$1" env="$2"
  shift 2
  stack_init "${stack}" "${env}"
  "${TF}" -chdir="${IAC_DIR}/stacks/${stack}" destroy -input=false \
    -var-file="env/${env}/terraform-${env}.tfvars" "$@" ${APPROVE[@]+"${APPROVE[@]}"}
}
