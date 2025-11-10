
read -d '' json
if [ "$json" = "[]" ];then 
   echo no rows returned by the query
   exit
fi

export header=`echo "$json" | jq '.[0] | keys'`
export fields=`echo "$header"|sed 's/",/,/g'|sed 's/"$//g'|sed 's/"/./g'`
echo $json|jq -r "($header),(.[]|$fields)|@tsv"|column -t -s $'\t'


