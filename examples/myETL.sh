#!/usr/bin/env bash
# examples/myETL.sh
# Demo ETL script that uses dataset-prefixed automatic environment variables.
# Usage (example): RunJob <decryptionkey> loadbob examples/myETL.sh
# 
# The RunJob wrapper is expected to provision environment variables like:
# This script expects an input dataset prefix (equal to bobin for this jopb)
# The prefix is always the the value of datasetid in job where jobid='loadbob'
#   bobin_hostname, bobin_database, bobin_username, bobin_schemaname, bobin_password
# dataset table stores encryptedpass (bobin_encryptedpass) but RunJob supplies decrypted password at runtime as bobin_password.

  export PGPASSWORD="$bobin_password"
  psql -U $bobin_username  -d $bobin_database -h $bobin_hostname -c "SELECT COUNT(*) FROM ${bobin_schemaname}.${bobin_tablename} WHERE partition_col = '$dataid';"
  export endstatus=$?

  echo "job $dataid is complete"

  exit $endstatus
  
