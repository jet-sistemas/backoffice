#!/usr/bin/env sh

set -eu

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
PROJECT_ROOT="$(CDPATH= cd -- "$SCRIPT_DIR/../../.." && pwd)"
ENV_FILE="$PROJECT_ROOT/.env.dev"

if [ ! -f "$ENV_FILE" ]; then
  echo "Arquivo .env.dev não encontrado em: $ENV_FILE" >&2
  exit 1
fi

cd "$PROJECT_ROOT"

set -a
. "$ENV_FILE"
set +a

exec ./mvnw quarkus:dev
