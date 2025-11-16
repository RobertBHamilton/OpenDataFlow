
read -d '' json
if [ "$json" = "[]" ];then 
   echo no rows returned by the query
   exit
fi

# occasionally we need to flatten and in one case step on the first pair 
#json="`echo "$json"|sed 's/},{/,/g' `"

export header=`echo "$json" | jq '.[0] | keys'`
export fields=`echo "$header"|sed 's/",/,/g'|sed 's/"$//g'|sed 's/"/./g'`

export tbl="`echo $json|jq -r "($header),(.[]|$fields)|@tsv"|column -t -s $'\t'`"

# get a line of correct length
line1=`echo "$tbl"|head -1`
line=`echo -e "$line1"|sed 's/[^ ]/-/g'`

# insert at line 2
echo -e "$tbl"|sed "2i$line"

