export CLASSPATH=bin/postgresql-42.7.3.jar:bin/json-20250517.jar:bin/h2-2.2.224.jar:utility/target/utility-1.0.0.jar
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


if [ -z "$PASSKEY" ];then
	export PASSKEY=$1
	shift
fi
cmd=$1
shift
# useful for debug  
#echo passkey $PASSKEY
#echo command $cmd
#echo args "$@"
export args="$@"
export util="com.hamiltonlabs.dataflow.utility"
export jarc="java -jar utility/target/utility-1.0.0.jar $PASSKEY  "
export jar="$jarc $cmd "
# Special case the runsql and other SQLs because we can and should make the result readable
# cmd now has arguments: $PASSKEY $cmd $args
case  "$cmd" in 
    "sql" ) 
	$jar "$@" |tail -1|./tablemaker.sh
        ;;
    "dml" ) 
	$jar "$@"  |./tablemaker.sh
        ;;
    "crypt" )
	$jar "$@"
        ;;
    "runs" ) 
	$jar |tail -1|./tablemaker.sh
        ;; 
    "getjob" ) 
	$jar  $@ |jq .
        ;; 
    "startjob" ) 
	$jar  $@ |tablemaker.sh jsonalso
        ;; 
    "endjob" )
	$jar $@ |tablemaker.sh
	;;
    "forcejob" ) 
	$jar  $@ 
        ;; 

    "jobs" )
	$jar |tail -1|./tablemaker.sh
        ;; 
    "deleterun" )
	$jar $@  |./tablemaker.sh
        ;; 
    "datasets" )
	$jar $@  |./tablemaker.sh
        ;; 
    *)
        java $util.$cmd $PASSKEY $@
        ;;
esac

