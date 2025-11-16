package com.hamiltonlabs.dataflow.utility;

/** Combined utilities in one executable jar 
 * Called always with dataflow key as first arg and command as second
 * To add a new utility, create a new main class in the utility module and call it from here
 */

public class util {

    public static void help(){
	System.out.println("""
  Usage: 
    sql     passkey \"sql for select\"
    dml     passkey \"sql for dml \"
    getjob  passkey jobid 
    setjob  passkey jobid dataid status CAUTION this changes dataflow status data 
    setstat passkey status dataid datasetid jobid locktype CAUTION this changes dataflow status data 
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

	    case "getjob":
		result=GetJobData.run(args[0],args[2]);
		break;

	    case "setjob":
		result=SetJobEndStatus.run(args[0],args[2],args[3],args[4]);
		break;

	    case "datasets":
		result=GetDataSets.run(args[0]);
	        break;

	    case "jobs":
		result=GetJobs.run(args[0]);
	        break;

	    case "startjob":
		result=LaunchJob.run(args[0],args[2]);
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
