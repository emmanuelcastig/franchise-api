#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

ENV="${1:-}"
require_env "${ENV}"
IMAGE_TAG="${2:-$(git -C "${REPO_DIR}" rev-parse --short=12 HEAD)}"

if [[ -z "${2:-}" ]] && ! git -C "${REPO_DIR}" diff --quiet HEAD --; then
  echo "The working tree has uncommitted changes; commit them so the image tag matches its source." >&2
  exit 1
fi

if command -v docker >/dev/null 2>&1; then
  RUNTIME="docker"
elif command -v podman >/dev/null 2>&1; then
  RUNTIME="podman"
else
  echo "Neither docker nor podman is installed." >&2
  exit 1
fi

REPOSITORY="${CAPACITY}-${COUNTRY}-${FUNCTIONALITY}-${ENV}"
REGISTRY="$(account_id).dkr.ecr.${AWS_REGION}.amazonaws.com"
IMAGE="${REGISTRY}/${REPOSITORY}:${IMAGE_TAG}"

if aws ecr describe-images --region "${AWS_REGION}" --repository-name "${REPOSITORY}" \
    --image-ids imageTag="${IMAGE_TAG}" >/dev/null 2>&1; then
  echo "Image ${IMAGE} already exists; tags are immutable, skipping the build."
  exit 0
fi

aws ecr get-login-password --region "${AWS_REGION}" | "${RUNTIME}" login --username AWS --password-stdin "${REGISTRY}"
"${RUNTIME}" build --platform linux/amd64 -f "${REPO_DIR}/deployment/Dockerfile" -t "${IMAGE}" "${REPO_DIR}"
"${RUNTIME}" push "${IMAGE}"

echo "Pushed ${IMAGE}"
