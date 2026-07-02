#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

COMPOSE_FILE="docker-compose.native-local.yml"
ENV_FILE="native-local.env"
IMAGE="backoffice:native-local"

usage() {
  cat <<EOF
Uso: $(basename "$0") <comando>

Comandos:
  build     Compila binário native (container Mandrel) e constrói imagem Docker
  up        Sobe Postgres + MinIO + API native (build automático se imagem não existir)
  down      Para todos os serviços do stack native-local
  logs      Segue logs da API native
  smoke     Testa OpenAPI e login (email/senha via env SMOKE_EMAIL / SMOKE_PASSWORD)

Exemplo:
  cp native-local.env.example native-local.env
  ./scripts/native-local.sh up
EOF
}

ensure_env() {
  if [[ ! -f "$ENV_FILE" ]]; then
    cp native-local.env.example "$ENV_FILE"
    echo "Criado $ENV_FILE a partir do example."
  fi
}

build_native() {
  echo ">> Build native (pode levar vários minutos na primeira vez)..."
  ./mvnw -B package -Dnative \
    -Dquarkus.native.container-build=true \
    -DskipTests \
    -Dquarkus.native.native-image-xmx=6g
  docker build -f src/main/docker/Dockerfile.native-micro -t "$IMAGE" .
  echo ">> Imagem $IMAGE pronta."
}

image_exists() {
  docker image inspect "$IMAGE" >/dev/null 2>&1
}

cmd_up() {
  ensure_env
  if ! image_exists; then
    build_native
  fi
  docker compose -f "$COMPOSE_FILE" up -d
  echo ""
  echo "API native: http://localhost:8080"
  echo "Swagger:    http://localhost:8080/q/swagger-ui"
  echo "Frontend:   cp ../backoffice-front/.env.local.example ../backoffice-front/.env.local && cd ../backoffice-front && pnpm dev"
}

cmd_down() {
  docker compose -f "$COMPOSE_FILE" down
}

cmd_logs() {
  docker compose -f "$COMPOSE_FILE" logs -f backoffice-native
}

cmd_smoke() {
  ensure_env
  echo ">> OpenAPI..."
  curl -sf "http://localhost:8080/q/openapi" >/dev/null && echo "OK" || { echo "Falhou — API está rodando?"; exit 1; }
  if [[ -n "${SMOKE_EMAIL:-}" && -n "${SMOKE_PASSWORD:-}" ]]; then
    echo ">> Login..."
    curl -sf -X POST "http://localhost:8080/v1/auth" \
      -H "Content-Type: application/json" \
      -d "{\"email\":\"$SMOKE_EMAIL\",\"password\":\"$SMOKE_PASSWORD\"}" | grep -q accessToken \
      && echo "Login OK (accessToken presente)" \
      || { echo "Login falhou"; exit 1; }
  else
    echo "Login ignorado — defina SMOKE_EMAIL e SMOKE_PASSWORD para testar."
  fi
}

case "${1:-}" in
  build) build_native ;;
  up) cmd_up ;;
  down) cmd_down ;;
  logs) cmd_logs ;;
  smoke) cmd_smoke ;;
  *) usage; exit 1 ;;
esac
