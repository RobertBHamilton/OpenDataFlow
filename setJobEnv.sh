#!/bin/bash

# for dry run:    ./setJobEnv.sh passkey jobid 
# for environment source ./setJobEnv.sh passkey jobid 

export passkey=$1
export jobid=$2

# 
export CLASSPATH=bin/postgresql-42.7.3.jar:bin/json-20250517.jar:app/target/app-1.0.0.jar

getEnvJSON(){
  java com.hamiltonlabs.dataflow.utility.GetJobData $jobid $passkey 
}

decrypt() {
  java -cp app/target/app-1.0.0.jar com.hamiltonlabs.dataflow.utility.Cryptor -d $passkey "$1"
}

declareEnv(){
getEnvJSON|jq -c '.[] | to_entries[]' | while read -r entry; do
  key=$(echo "$entry" | jq -r '.key')
  value=$(echo "$entry" | jq -r '.value')

  # Emit export statement
  echo "declare -x $key=\"${value}\";"

  # Handle encrypted passwords
  if [[ "$key" =~ ^(.*)_encryptedpass$ ]]; then
    prefix="${BASH_REMATCH[1]}"
    decrypted=$(decrypt "$value")
    echo "declare -x ${prefix}_password=\"${decrypted}\";"
  fi
done
}

if [ "$3" = "dryrun" ];then 
  declareEnv
else 
  source <(declareEnv)
fi
