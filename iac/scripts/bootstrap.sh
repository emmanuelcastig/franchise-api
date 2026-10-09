#!/usr/bin/env bash
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

"${TF}" -chdir="${IAC_DIR}/bootstrap" init -input=false
"${TF}" -chdir="${IAC_DIR}/bootstrap" apply -input=false -var="region=${AWS_REGION}" ${APPROVE[@]+"${APPROVE[@]}"}

echo "State bucket: $("${TF}" -chdir="${IAC_DIR}/bootstrap" output -raw state_bucket_name)"
