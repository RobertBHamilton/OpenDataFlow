package com.hamiltonlabs.dataflow.utility;

/** Combined utilities in one executable jar 
 * Called always with dataflow key as first arg and command as second
 * To add a new utility, create a new main class in the utility module and call it from here
 */

public class util {

    public static void help(){
	System.out.println("""
  Usage: 
    sql       passkey \"sql for select\" -- run the specified select statement
    dml       passkey \"sql for dml \"   -- run specified update/delete/ddl statement
    getjob    passkey jobid              -- get status and registered data for the job
    datasets  passkey                    -- list all registered datasets
    jobs      passkey                    -- list all registered jobs
    runs      passkey                    -- list last 20 runs
    crypt     passkey -e text            -- encrypt (-e) or decrypt (-d)

CAUTION below changes dataflow status:

    endjob    passkey jobid dataid status -- end the job run with status
    startjob  passkey jobid               -- find a suitable data chunk  for this job lock it and register running
    forcejob  passkey jobid dataid        -- arbitrarily set a fake dataid for this job lock it and register running
    deleterun passkey jobid dataid
	""");
    }

    public static void main(String[] args) throws Exception{
	if (args.length<1){
		help();
		return;
	} 	
	String result;
	switch (args[1]) {
            case "sql":
                result=RunSql.run(args[0],args[2]);
                break; 

            case "dml":
	        result=RunUpdate.run(args[0],args[2]);
                break;

	    case "createtables":
		result=CreateTables.run(args[0]);
	        break;		

	    case "getjob":
		result=GetJobData.run(args[0],args[2]);
		break;

	    case "endjob":
		result=SetJobEndStatus.run(args[0],args[2],args[3],args[4]);
		break;

	    case "datasets":
		result=GetDataSets.run(args[0]);
	        break;

	    case "jobs":
		result=GetJobs.run(args[0]);
	        break;

	    case "forcejob":
		result=ForceJob.run(args[0],args[1],args[2]);
	        break;

	    case "forcejobts":
		result=ForceJobTS.run(args[0],args[2]);
	        break;


	    case "startjob":
		result=LaunchJob.run(args[0],args[2]);
	        break;

	    case "deleterun":
		result=DeleteRun.run(args[0],args[2],args[3]);
	        break;


	    case "runs":
		result=GetJobRuns.run(args[0]);
		break;

	    case "crypt":
		if (args.length==5){
                    /*e.g., passkey crypt -e key text */
		    result=Cryptor.run(args[3],args[2],args[4]); 
		} else {            
                   /*e.g. crypt -e passkey text */
		    result=Cryptor.run(args[0],args[2],args[3]);
		}	
	        break;

            default: 
		help();
		result="Unknown command: "+args[1];	
                break;
        }
	System.out.println(result);
    } 
}
