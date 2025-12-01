#!/bin/bash

export CLASSPATH=$CLASSPATH:bin/postgresql-42.7.3.jar:bin/json-20250517.jar:bin/h2-2.2.224.jar:utility/target/dataflow-1.0.0.jar
if [ $# -lt 1 ];then
cat<<DONE
usage: $0 cmd args
currently supported commands with required args are:

    utility.sh sql  <sql>         -- run a select sql statement
    utility.sh ddl  <sql>         -- run a ddl or update/delete statement 
    utility.sh crypt -e key text  -- -e to encrypt or -d to decrypt
    utility.sh runs               -- list the 20 most recent job runs
    utility.sh getjob             -- get input/output configuration for a job
    utility.sh jobs               -- list all jobs registered
    utility.sh datasets           -- list all datasets registered
    utility.sh forcejob           -- get a timestamp dataid even if no dependencies are met.  But see  **
    utility.sh createtables       -- create the job,dataset, and datastatus tables

    CLASSNAME args -- any executable class in utilities module. Example:
     utility.sh  endjob jobid dataid status 
     utility.sh  GetJobData jobid 
     utility.sh  SetDataStatus RUNNING 3000 today newjob OUT
     utility.sh dml "delete from datastatus where jobid='newjob'a
** forcejob breaks every contract. Just use it to verify your configuration during dev. ** 

if the PASSKEY environment variable is set then you can omit passkey from the arguments above.
    Example: 
	export PASSKEY=plugh 
	utility.sh sql "select * from datastatus"

if PASSKEY is not set, the passkey is expected as the FIRST argument to utility.sh, followed by the command and then the args
    Example: 
	utility.sh plugh sql "select * from datastatus"
DONE
    exit
fi

# A helper function that converts JSON string into a table format like a typical SQL output
# if it cant be parsed then print the string and go away
function tablemaker(){
    read -d '' json
    if [ "$json" = "[]" ];then 
       echo no rows returned by the query
       exit
    fi
    if [ -z "$json" ];then 
       echo no rows returned by the query
       exit
    fi


    # First a simple test to see if it is valid JSON
    echo "$json"|jq >/dev/null 2>&1
    if [ $?  -gt 0 ]; then echo "$json";exit;fi

    # else if there is a switch to pretty print the json also the do it
    if [ ! -z "$1" ];then
        echo "$json" | jq
    fi

    export header=`echo "$json" | jq '.[0] | keys'`  2>/dev/null
    export fields=`echo "$header"|sed 's/",/,/g'|sed 's/"$//g'|sed 's/"/./g'`
    export tbl="`echo $json|jq -r "($header),(.[]|$fields)|@tsv"|column -t -s $'\t'`"

    # get a line of correct length
    line1=`echo "$tbl"|head -1`
    line=`echo -e "$line1"|sed 's/[^ ]/-/g'`

    # insert at line 2
    echo -e "$tbl"|sed "2i$line"
}

if [ -z "$PASSKEY" ];then
	export PASSKEY=$1
	shift
fi
cmd=$1
shift

export args="$@"
export util="com.hamiltonlabs.dataflow.utility"
# get the jar file either in current directory or in canonical build location
jarfile="`ls  utility/target/dataflow*jar dataflow*jar 2>/dev/null|tail -1`"
if [ -f "$jarfile" ];then
	export jarc="java -jar $jarfile $PASSKEY "
else
	echo we need the dataflow-version.jar to run this utility. Cannot continue
	exit
fi
export jar="$jarc $cmd "

# Special case the runsql and other SQLs because we can and should make the result readable
# cmd now has arguments: $PASSKEY $cmd $args
case  "$cmd" in 
    "sql" ) 
	$jar "$@" |tail -1|tablemaker
        ;;
    "dml" ) 
	$jar "$@"  |tablemaker
        ;;
    "crypt" )
	$jar "$@"
        ;;
    "runs" ) 
	$jar |tail -1|tablemaker
        ;; 
    "getjob" ) 
	$jar  $@ |jq .
        ;; 
    "startjob" ) 
	$jar  $@ |tablemaker jsonalso
        ;; 
    "endjob" )
	$jar $@ |tablemaker
	;;
    "forcejob" ) 
	$jar  $@ 
        ;; 
    "createtables" )
	$jar  $@ |tablemaker
	;;
    "jobs" )
	$jar |tail -1|tablemaker
        ;; 
    "deleterun" )
	$jar $@  |tablemaker
        ;; 
    "datasets" )
	$jar $@  |tablemaker
        ;; 
    *)
        java $util.$cmd $PASSKEY $@
        ;;
esac



