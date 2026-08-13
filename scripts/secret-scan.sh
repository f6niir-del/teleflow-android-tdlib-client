#!/usr/bin/env bash
set -euo pipefail

cd "$(git rev-parse --show-toplevel)"

# Search tracked content only, avoiding false positives from documentation placeholders.
patterns=(
  'gh[pousr]_[A-Za-z0-9_]{20,}'
  'github_pat_[A-Za-z0-9_]{20,}'
  'AKIA[0-9A-Z]{16}'
  'AIza[0-9A-Za-z_-]{20,}'
  'sk-[A-Za-z0-9]{20,}'
  '-----BEGIN (RSA |EC |OPENSSH )?PRIVATE KEY-----'
  'Bearer[[:space:]]+[A-Za-z0-9._-]{20,}'
  'TELEGRAM_API_HASH[[:space:]]*=[[:space:]]*[0-9a-fA-F]{16,}'
)

failed=0
for pattern in "${patterns[@]}"; do
  if git grep -n -I -E "$pattern" -- ':!scripts/secret-scan.sh' ':!PROJECT_PROMPT.md'; then
    failed=1
  fi
done

# Guard against accidentally tracked local configuration and signing material.
for forbidden in local.properties teleflow.properties secrets.properties credentials.properties .env; do
  if git ls-files --error-unmatch "$forbidden" >/dev/null 2>&1; then
    echo "Forbidden tracked configuration: $forbidden" >&2
    failed=1
  fi
done

if (( failed )); then
  echo "Secret scan failed." >&2
  exit 1
fi

echo "Secret scan passed: no high-confidence credential patterns or forbidden local files found in tracked content."
