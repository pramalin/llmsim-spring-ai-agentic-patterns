#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${ROOT_DIR}/compose.yaml"
LLMSIM_PORT="${LLMSIM_PORT:-8089}"
LLMSIM_BASE_URL="${LLMSIM_BASE_URL:-http://localhost:${LLMSIM_PORT}}"

wait_for_llmsim() {
  local attempts=0

  until curl --fail --silent \
    "${LLMSIM_BASE_URL}/_llmsim/status" >/dev/null; do
    attempts=$((attempts + 1))

    if [[ "${attempts}" -ge 120 ]]; then
      echo "llmsim did not become ready." >&2
      docker compose -f "${COMPOSE_FILE}" logs llmsim >&2 || true
      exit 1
    fi

    sleep 0.5
  done
}

start_scenario() {
  local script_class="$1"

  echo
  echo "Starting ${script_class}"

  LLMSIM_SCRIPT="${script_class}" \
  LLMSIM_PORT="${LLMSIM_PORT}" \
    docker compose -f "${COMPOSE_FILE}" \
      up --build --detach --force-recreate llmsim

  wait_for_llmsim

  curl --fail --silent --request POST \
    "${LLMSIM_BASE_URL}/_llmsim/reset" >/dev/null
}

run_test() {
  local module="$1"
  local test_class="$2"

  LLMSIM_BASE_URL="${LLMSIM_BASE_URL}" \
    mvn --batch-mode --no-transfer-progress \
      -pl "${module}" \
      -Dtest="${test_class}" \
      test
}

cleanup() {
  LLMSIM_PORT="${LLMSIM_PORT}" \
    docker compose -f "${COMPOSE_FILE}" down --remove-orphans
}

trap cleanup EXIT

start_scenario "com.example.agentic.llmsim.ChainWorkflowFlow"
run_test "chain-workflow" "ChainWorkflowLlmsimTest"

start_scenario \
  "com.example.agentic.llmsim.ParallelizationWorkflowFlow"
run_test \
  "parallelization-workflow" \
  "ParallelizationWorkflowLlmsimTest"

start_scenario "com.example.agentic.llmsim.RoutingWorkflowFlow"
run_test "routing-workflow" "RoutingWorkflowLlmsimTest"

start_scenario \
  "com.example.agentic.llmsim.OrchestratorWorkersFlow"
run_test \
  "orchestrator-workers" \
  "OrchestratorWorkersLlmsimTest"

start_scenario \
  "com.example.agentic.llmsim.EvaluatorOptimizerFlow"
run_test \
  "evaluator-optimizer" \
  "EvaluatorOptimizerLlmsimTest"

echo
echo "All five llmsim agentic-pattern demonstrations passed."
