#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

ENV="${1:-}"
require_env "${ENV}"

stack_destroy franchise-api "${ENV}" -var="container_image_tag=destroy"
stack_destroy transversal "${ENV}"

echo "Environment ${ENV} destroyed. The state bucket is kept; remove it with: terraform -chdir=iac/bootstrap destroy -var=force_destroy=true"
