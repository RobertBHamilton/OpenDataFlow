#!/bin/sh 

# Validation rule: dataset is valid if and only if number of records > 1


# This is the version to run against H2 
# Note that RunJob put us in the right directory, which also has utility.sh
# verify this:
echo Current working directory `pwd`
./utility.sh sql "select count(*) as numrows from ${datastatus_schemaname}.${datastatus_tablename} where cast(modified as date) = parsedatetime('$dataid','yyyy-MM-dd')" 

export n=`./utility.sh sql "select count(*) as numrows from ${datastatus_schemaname}.${datastatus_tablename} where cast(modified as date) = parsedatetime('$dataid','yyyy-MM-dd')" |head -3|tail -1`

echo "dataset hass $n rows"
if [ "$n" -ge 0 ];then 
    echo "n=$n valid"
    exit 0 
else
    
    echo "n=$n invalid"
    exit 1
fi
#No statements after this if you want dataflow to caputure the run status

