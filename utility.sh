export CLASSPATH=bin/postgresql-42.7.3.jar:bin/json-20250517.jar:app/target/app-1.0.0.jar
if [ $# -lt 1 ];then
cat<<DONE
usage: $0 cmd args
currently supported commands with required args are:

	SetJobEndStatus  passkey jobid dataid status 
	    Set the status of the jobs output data at the end of the job
	    passkey is key used to encrypt the dataflow password in dataflow.properties
	    jobid is the registered job ID in the dataflow job table
	    dataid is the run sequence. Could be any string but should be sortable 
	    status is one of RUNNING,FAILED,READY,RESUBMIT

	Cryptor  [-d|-e] passkey text   
  	     -d  decrypts the text.  
             -e encrypts the text

        GetJobData jobid passkey 
	     NOTE THIS HAS SIDE EFFECTS. DO NOT USE in this version

	RunUpdate passkey sql 
        RunSql passkey sql

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
# Special case the runsql because we can and should make the result readable
case  "$cmd" in 
    "RunSql"|"RunUpdate" ) 
        java com.hamiltonlabs.dataflow.utility.$cmd $PASSKEY "$@"|./tablemaker.sh
        ;;
    *)
        java com.hamiltonlabs.dataflow.utility.$cmd $PASSKEY $@
        ;;
esac

