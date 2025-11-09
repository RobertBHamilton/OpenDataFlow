#!/bin/bash

# Usage: cat input.json | ./emit_env.sh | source /dev/stdin

decrypt() {
  echo "decrypted_$1"  # Replace with your actual decrypt logic
}

jq -c '.[] | to_entries[]' | while read -r entry; do
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

