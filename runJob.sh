#!/bin/bash
#
# Look for an environment registered in DataFlow, if a suitable data chunk is available then
# Set the environment and execute the command in a new process
# If not then drop a message and exit 

export passkey=$1
export jobid=$2
export cmd=$3
# 
export CLASSPATH=bin/postgresql-42.7.3.jar:bin/json-20250517.jar:app/target/app-1.0.0.jar

getEnvJSON(){
  java com.hamiltonlabs.dataflow.utility.GetJobData $jobid $passkey 
}

decrypt() {
  java -cp app/target/app-1.0.0.jar com.hamiltonlabs.dataflow.utility.Cryptor -d $passkey "$1"
}

# Because we pipe to the parse, it creates a separate process and so any changes we make
# to the environment will vanish when the process exits. Instead we just pipe it to source 
# using Bash process substitution
declareEnv(){
  #stale dataid will result in false executions so make sure we only set it here
  unset dataid   
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

env="`declareEnv`"
# uncomment if you need to debug echo "$env"
source <(echo "$env")
if [ -z "$dataid" ];then
  echo "`date`: no suitable data available for job. Not running it"
  exit
fi

echo "`date`: Launching $cmd with dataid $dataid"
eval "$cmd"
 if [ $? -eq 0 ];then
     echo "`date`: Job $cmd is complete. Updating status"
     java -cp $CLASSPATH:app.jar com.hamiltonlabs.dataflow.utility.SetJobEndStatus $passkey $jobid $dataid READY
else
     echo "`date`: Job $cmd has failed. Updating status"
     java -cp $CLASSPATH:app.jar com.hamiltonlabs.dataflow.utility.SetJobEndStatus $passkey $jobid $dataid FAILED
fi

