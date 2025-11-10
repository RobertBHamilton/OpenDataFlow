#!/usr/bin/env bash
# examples/myETL.sh
# Demo ETL script that uses dataset-prefixed automatic environment variables.
# Usage (example): RunJob examples/myETL.sh
# The RunJob wrapper is expected to provision environment variables like:
#   bobin_hostname, bobin_database, bobin_username, bobin_schemaname, bobin_password
#   bobout_hostname, bobout_database, bobout_username, bobout_schemaname, bobout_password
# Dataset table stores encryptedpass (bobin_encryptedpass) but RunJob supplies decrypted password at runtime as bobin_password.
# This script expects an input dataset prefix (default: bobin) and an output dataset prefix (default: bobout).

set -euo pipefail

IN_PREFIX="${1:-bobin}"
OUT_PREFIX="${2:-bobout}"

read_var() {
  local prefix="$1" name="$2" var="${prefix}_${name}"
  echo "${!var:-}"
}

IN_HOST="
"IN_HOST="$(read_var "$IN_PREFIX" hostname)"
IN_DB="$(read_var "$IN_PREFIX" database)"
IN_SCHEMA="$(read_var "$IN_PREFIX" schemaname)"
IN_TABLE="$(read_var "$IN_PREFIX" tablename)"
IN_USER="$(read_var "$IN_PREFIX" username)"
IN_PASS="$(read_var "$IN_PREFIX" password)"
# dataset table may contain encryptedpass; the runtime may provide decrypted password as *_password
if [ -z "${IN_PASS}" ]; then
  IN_PASS="$(read_var "$IN_PREFIX" encryptedpass || true)"
fi

OUT_HOST="$(read_var "$OUT_PREFIX" hostname)"
OUT_DB="$(read_var "$OUT_PREFIX" database)"
OUT_SCHEMA="$(read_var "$OUT_PREFIX" schemaname)"
OUT_TABLE="$(read_var "$OUT_PREFIX" tablename)"
OUT_USER="$(read_var "$OUT_PREFIX" username)"
OUT_PASS="$(read_var "$OUT_PREFIX" password)"
if [ -z "${OUT_PASS}" ]; then
  OUT_PASS="$(read_var "$OUT_PREFIX" encryptedpass || true)"
fi

DATAID="${dataid:-${DATAID:-}}"

if [ -z "$DATAID" ]; then
  echo "ERROR: partition id not provided in dataid/DATAID" >&2
  exit 2
fi

echo "=== Example ETL run ==="
echo "Partition: $DATAID"
echo "Input dataset prefix: $IN_PREFIX (host=${IN_HOST:-'(none)'} schema=${IN_SCHEMA:-'(none)'} table=${IN_TABLE:-'(none)'})"
echo "Output dataset prefix: $OUT_PREFIX (host=${OUT_HOST:-'(none)'} schema=${OUT_SCHEMA:-'(none)'} table=${OUT_TABLE:-'(none)'})"

echo "NOTE: this example script does not manipulate locks — RunJob/dataflow control plane handles locking semantics."

if [ -n "$IN_DB" ] && [ -n "$IN_USER" ] && [ -n "$IN_PASS" ]; then
  echo "Querying input dataset for partition $DATAID (demo count)..."
  export PGPASSWORD="$IN_PASS"
  psql "$IN_DB" -U "$IN_USER" -c "SELECT COUNT(*) FROM ${IN_SCHEMA}.${IN_TABLE} WHERE partition_col = '$DATAID';" || echo "(psql returned non-zero; this is a demo)"
  unset PGPASSWORD
else
  echo "Skipping DB query: missing in-dataset connection info (ok for demo)."
fi

echo "Simulating ETL work for partition $DATAID..."
sleep 1
echo "ETL work completed for $DATAID"

echo "Exit 0 (success)"
exit 0
