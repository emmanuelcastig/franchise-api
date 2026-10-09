#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

ENV="${1:-}"
require_env "${ENV}"
IMAGE_TAG="$(git -C "${REPO_DIR}" rev-parse --short=12 HEAD)"

stack_apply transversal "${ENV}"
"${IAC_DIR}/scripts/publish-image.sh" "${ENV}"
stack_apply franchise-api "${ENV}" -var="container_image_tag=${IMAGE_TAG}"

API_URL="$("${TF}" -chdir="${IAC_DIR}/stacks/franchise-api" output -raw api_url)"
echo "FRANCHISE_API_URL=${API_URL}" > "${REPO_DIR}/docs/bruno/.env"
echo
echo "API:     ${API_URL}"
echo "Swagger: ${API_URL}/swagger-ui.html"
echo "Health:  ${API_URL}/actuator/health/readiness"
