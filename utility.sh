export CLASSPATH=bin/postgresql-42.7.3.jar:bin/json-20250517.jar:app/target/app-1.0.0.jar
if [ $# -lt 1 ];then
cat<<DONE
usage: $0 cmd args
currently supported commands with required args are:

	SetJobEndStatus  passkey jobid dataid status 
	Cryptor  [-d|-e] passkey text   
        GetJobData jobid passkey 
	RunUpdate passkey sql 
        RunSql passkey sql
        RunSql passkey sql
        SetDataStatus passkey dataid datasetid jobid lockType status

if the PASSKEY environment variable is set then you can omit passkey from the arguments above	
DONE
    exit
fi


if [ -z "$PASSKEY" ];then
	export PASSKEY=$1
	shift
fi
cmd=$1
shift
echo passkey $PASSKEY
echo command $cmd
echo args $@
# Special case the runsql because we can and should make the result readable
case  "$cmd" in 
    "RunSql"|"RunUpdate" ) 
        java com.hamiltonlabs.dataflow.utility.$cmd $PASSKEY "$@"|./tablemaker.sh
        ;;
    "Cryptor" )
        # out of order args for cryptor 
	java com.hamiltonlabs.dataflow.utility.$cmd $1 $PASSKEY $2
        ;;
    *)
        java com.hamiltonlabs.dataflow.utility.$cmd $PASSKEY $@
        ;;
esac

