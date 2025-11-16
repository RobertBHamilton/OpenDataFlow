export CLASSPATH=bin/postgresql-42.7.3.jar:bin/json-20250517.jar:utility/target/utility-1.0.0.jar
if [ $# -lt 1 ];then
cat<<DONE
usage: $0 cmd args
currently supported commands with required args are:

    utility.sh sql  <sql>        -- run a select sql statement
    utility.sh dml  <sql>        -- run a dml statement 
    utility.sh crypt -e key text  -- -e to encrypt or -d to decrypt
    utility.sh runs               -- list the 20 most recent job runs
    utility.sh getjob             -- get input/output configuration for a job
    utility.sh jobs               -- list all jobs registered
    utility.sh datasets           -- list all datasets registered

    CLASSNAME args -- any executable class in utilities module. Example:
     utility.sh  SetJobEndStatus jobid dataid status 
     utility.sh  GetJobData jobid 
     utility.sh  SetDataStatus RUNNING 3000 today newjob OUT
     utility.sh dml "delete from datastatus where jobid='newjob'a

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

case  "$cmd" in 
    "sql" ) 
	$jar "$@" |./tablemaker.sh
        ;;
    "dml" ) 
	$jar "$@"  |./tablemaker.sh
        ;;
    "crypt" )
	$jar "$@"
        ;;
    "runs" ) 
	$jar |./tablemaker.sh
        ;; 
    "getjob" ) 
	$jar  $@ |jq .
        ;; 
    "jobs" )
	$jar |./tablemaker.sh
        ;; 
    "datasets" )
	$jar $@  |./tablemaker.sh
        ;; 
    *)
        java $util.$cmd $PASSKEY $@
        ;;
esac

