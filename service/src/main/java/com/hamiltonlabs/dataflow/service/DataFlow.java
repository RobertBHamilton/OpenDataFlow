package com.hamiltonlabs.dataflow.service;

import com.hamiltonlabs.dataflow.core.*;
import java.util.List;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
/** DataFlow exoses all the serviced needed to support the full job lifecycle.
  * getJobData provides all the runtime configuration needed for the data sets used by the job
  * startJob gets global rw lock on the output dataset and job-local locks on all input sets
  * endJob sets final status on output dataset and releases job-local locks 
  * All lock operations are built on setStatus which upserts rows into datastatus table.
  */

public class DataFlow {
  
   public DataFlow(){
   }

  /** create tables for the dataflow schema  */
  public static String createTables(String passkey)throws Exception{
         DataProvider dataprovider=new DataProvider().open(passkey,"dataflow.properties");
	  
	int updatecount=0;
	updatecount+=dataprovider.runUpdate(dataprovider.getSQL("createJobSQL"));
	updatecount+=dataprovider.runUpdate(dataprovider.getSQL("createDatasetSQL"));
	updatecount+=dataprovider.runUpdate(dataprovider.getSQL("createDatastatusSQL"));
	updatecount+=dataprovider.runUpdate(dataprovider.getSQL("createIndexDatasetSQL"));
        return String.format("[{\"result\":\"tables/indexes created\"}]");
  }
    /**  Set the status for a given dataset,job, and chunk 
     *
     *  @param datasetid    string representing registered dataset
     *  @param jobid        string representing registered job ID
     *  @param dataid       string representing the data chunk
     *  @param dataprovider the dataprovider used for the dataflow database
     *  @param locktype     the type of lock to set, either OUT or IN
     *  @param status       the status to set 
     */
    public static void setStatus(String datasetid,String jobid,String dataid,DataProvider dataprovider,String locktype,String status) throws Exception {
	/* insert should have the following fields:  datasetid |  jobid   | dataid | locktype | status            */
	
	int updatecount=dataprovider.runUpdate(dataprovider.getSQL("updateSQL"),status,locktype,dataid,datasetid,jobid);
	if (updatecount==0){
	    updatecount=dataprovider.runUpdate(dataprovider.getSQL("insertSQL"),datasetid,jobid,dataid,locktype,status);
        }
	if (updatecount==0){ 
	    throw new SQLException(String.format("Upsert for job %s and dataset %s could not be performed",jobid,datasetid));
	}
    }
    /* Called at the end of the job. We ensure the joblocal locks are released by deleting all the job's IN rows from datastatus */
    public static String setJobEndStatus(String passkey,String jobid,String dataid,String status) throws Exception{
	String returnString="";
         DataProvider dataprovider=new DataProvider().open(passkey,"dataflow.properties");
	/* set the OUT status */

	int updatecount=dataprovider.runUpdate(dataprovider.getSQL("updateOutStatusSQL"),status,jobid,dataid);
        int deletecount=dataprovider.runUpdate(dataprovider.getSQL("updateFileLocalStatusSQL"),jobid,dataid);

	 returnString=String.format("%d rows updated to %s for %s and %s",updatecount,status,jobid,dataid);
         returnString=returnString+String.format(" %d IN file-local locks released",deletecount);

	return returnString;
    }

    /** just force the job to run with the given dataid
     *  This ignores any and all upstream dependencies. We do this only for testing, dev and demo
     *  The return value includes data descriptors for input/output data sets which might be useful for examination
     */
    public static String forceJobTS(String passkey,String jobid) throws Exception{
	String dataid=LocalDateTime.now().toString();
	return forceJob(passkey,jobid,dataid);
    }

    /** just force the job to run. We do this by demanding a dataid down to the nanosecond
     *  This ignores any and all upstream dependencies. We do this only for testing, dev and demo
     *  The return value includes data descriptors for input/output data sets which might be useful for examination
     */
    public static String forceJob(String passkey,String jobid,String dataid)throws Exception {
        DataProvider dataprovider=new DataProvider().open(passkey,"dataflow.properties");
	JSONArray result=new JSONArray();
        JSONObject obj = new JSONObject();
	ResultSet rs;
        obj.put("dataid",dataid);
        result.put(obj);


        /* If we get  here we have found a dataset, have a dataid value for it, and a lock on the row */
        /* now get the data to return and set the locks */
        rs=dataprovider.runSQL(dataprovider.getSQL("datasetSQL"),jobid);
        ResultSetMetaData rsmd = rs.getMetaData();
        while(rs.next()) {
            int numColumns = rsmd.getColumnCount();
            obj = new JSONObject();
            String datasetid=rs.getString("datasetid");
            String locktype=rs.getString("itemtype");
            setStatus(datasetid,jobid,dataid,dataprovider,locktype,"RUNNING");
            String prefix=datasetid;
            /* flatten the variable namespace for the scripts to use */
            if (prefix != null){ prefix=prefix+"_"; }
            for (int i=1; i<=numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                obj.put(prefix+column_name, rs.getObject(column_name));
            }
            result.put(obj);
        }
        return result.toString();
     }

    /* This performs row level locking for update so both the get data and the updates are done in one transaction */
    public static String launchJob(String passkey,String jobid)throws Exception{

        DataProvider dataprovider=new DataProvider().open(passkey,"dataflow.properties");

	int bt=dataprovider.runUpdate(dataprovider.getSQL("beginTransactionSQL"));
	String lockStatusSQL=dataprovider.getSQL("lockStatusSQL");
	if (!lockStatusSQL.equals("")){
	   int k=dataprovider.runUpdate("lock datastatus in access exclusive mode ");
	}
	String dataid;
	JSONArray result=new JSONArray();
        JSONObject obj = new JSONObject();

	ResultSet rs=dataprovider.runSQL(dataprovider.getSQL("dataidTodaySQL"),jobid,jobid);
	/* if no automatic result then do the check for normal data set input */
	if (rs.next()){  
	    dataid=rs.getString("dataid");
	}else {
            rs=dataprovider.runSQL(dataprovider.getSQL("dataidLockedSQL"),jobid);
	    if (rs.next()){
	    dataid=rs.getString("dataid");
	   } else {
	      return result.toString();  /* empty set */
	   }
        }

        obj.put("dataid",dataid);
	result.put(obj);
	/* If we get  here we have found a dataset, have a dataid value for it, and a lock on the row */
        /* now get the data to return and set the locks */ 
	rs=dataprovider.runSQL(dataprovider.getSQL("datasetSQL"),jobid);
        ResultSetMetaData rsmd = rs.getMetaData();
        while(rs.next()) {
            int numColumns = rsmd.getColumnCount();
            obj = new JSONObject();
	    String datasetid=rs.getString("datasetid");
	    String locktype=rs.getString("itemtype");
	    setStatus(datasetid,jobid,dataid,dataprovider,locktype,"RUNNING");
            String prefix=datasetid;
	    /* flatten the variable namespace for the scripts to use */
            if (prefix != null){ prefix=prefix+"_"; }
            for (int i=1; i<=numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                obj.put(prefix+column_name, rs.getObject(column_name));
            }
            result.put(obj);
        }

	int et=dataprovider.runUpdate(dataprovider.getSQL("endTransactionSQL"));
        return result.toString();
     }
 
    /** Get next available data chunk for the job
     *
     *  Provides dataset information for each dataset registered to this job, dataid which is to be interpreted as a slice of data to process, 
     *  other optional environment variables if registered.
     *   
     *  @param jobid the identifier of the job. 
     *  @param passkey the credential needed to access the service
     * 

/* Add or update the data status for this job's datasets */
/* this is dangerous when called from user because it will get into a race if he hammers it */

    public static void setStartStatus(String passkey,String jobid,String dataid)throws Exception{
        DataProvider dataprovider=new DataProvider().open(passkey,"dataflow.properties");
	ResultSet rs=dataprovider.runSQL(dataprovider.getSQL("datasetSQL"),jobid);
        ResultSetMetaData rsmd = rs.getMetaData();
        while(rs.next()) {
            int numColumns = rsmd.getColumnCount();
	    String datasetid=rs.getString("datasetid");
	    String locktype=rs.getString("itemtype");
	    setStatus(datasetid,jobid,dataid,dataprovider,locktype,"RUNNING");
            }
    }

 
    /** Get next available data chunk for the job
     *
     *  Provides dataset information for each dataset registered to this job, dataid which is to be interpreted as a slice of data to process, 
     *  other optional environment variables if registered.
     *   
     *  @param jobid the identifier of the job. 
     *  @param passkey the credential needed to access the service
     * 
     *  @returns a json string representing the runtime configuration 
     *  This would would be called from a utility
     * We are stepping on double quotes because downstream we a manipulating the results in shell script
     *  using jq, and trying to properly escape them is impossible
     *  TODO: put a java tablebuilder in utility module and then remove this corruption
     */
    
    public static String getJobData(String passkey,String jobid) throws Exception{
	try {
            DataProvider dataprovider=new DataProvider().open(passkey,"dataflow.properties");
	    return getJobData(jobid,dataprovider);
	} catch (SQLException |GeneralSecurityException|java.io.IOException e){
          return String.format("[{\"result\":\"%s\"}]",e.getMessage().replaceAll("\"","'"));
	}
    }

/** Run an arbitrary DDL SQL, returning rows affected */
    public static int runUpdate(String passkey,String ... vars)throws Exception{	
        DataProvider p=new DataProvider().open(passkey,"dataflow.properties");
        return p.runUpdate(vars);
    }

/** Run an arbitrary DDL SQL. Opens a new DataProvider. This is typically used by utitilies which do one-off commands */
     public static String runUpdate(String passkey,String sqltext){	
	try{
            DataProvider p=new DataProvider().open(passkey,"dataflow.properties");
	    return runUpdate(p,sqltext);
	} catch (SQLException |GeneralSecurityException|java.io.IOException e){
            return String.format("[{\"result\":\"%s\"}]",e.getMessage().replaceAll("\"","'"));
        }

    }

/** Run an arbitrary DML SQL on the DataProvider. Typically used by tests and functions which require multiple steps */
     public static String runUpdate(DataProvider p,String sqltext){	
	try{
          return String.format("[{\"result\":\"%d rows affected\"}]",p.runUpdate(sqltext));
	} catch (SQLException e){
          return String.format("[{\"result\":\"%s\"}]",e.getMessage().replaceAll("\"","'"));
	}
    }

/** Run an arbitrary SQL. Accepts an open DataProvider */
    public static String runSql(String passkey,String sqltext){	
        try{     
              DataProvider p=new DataProvider().open(passkey,"dataflow.properties");
              return runSql(p,sqltext);
	} catch (SQLException |GeneralSecurityException|java.io.IOException e){
              return String.format("[{\"result\":\"%s\"}]",e.getMessage().replaceAll("\"","'").replaceAll("\n",""));
	}
     }

     public static String runUpdate(DataProvider p,String ... args){	
	try{
          return String.format("[{\"result\":\"%d rows affected\"}]",p.runUpdate(args));
	} catch (SQLException e){
          return String.format("[{\"result\":\"%s\"}]",e.getMessage().replaceAll("\"","'"));
	}
    }

/** Run an arbitrary SQL. Opens a data DataProvider */

    public static String runSql(DataProvider p,String sqltext){	
        try{     
  	      return DataFlow.rs2String(p.runSQL(sqltext));
	} catch (SQLException  e){
              return String.format("[{\"result\":\"%s\"}]",e.getMessage().replaceAll("\"","'").replaceAll("\n",""));
	}
     }

    /** get next available data chunk for the jobs
     *  this one would be called from this class where an open dataprover exists
     */
    public static String getJobData(String jobid,DataProvider dataprovider) throws Exception{

         /*  First query gets a dataid for the data. If there is one, then the second query gets every data set configuration.  
          *  Then we get any additional env items that were registered for the job.
          *  We munge it all together into a json object  and return it
          * TODO: Migrate sql to a resource 
          */

	String dataidSQL=dataprovider.getSQL("dataidSQL");
	String datasetSQL=dataprovider.getSQL("datasetSQL");
	ResultSet rs=dataprovider.runSQL(dataidSQL,jobid);
	String dataid;
	JSONArray result=new JSONArray();
	if (rs.next()){
	   dataid=rs.getString("dataid");
           JSONObject obj = new JSONObject();
           obj.put("dataid",dataid);
	   result.put(obj);
	} else {
	   return result.toString();  /* empty set */
	}
	/* If we get  here we have found a dataset. */
        
	rs=dataprovider.runSQL(datasetSQL,jobid);
       ResultSetMetaData rsmd = rs.getMetaData();
        while(rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();
	    String datasetid=rs.getString("datasetid");
	    String locktype=rs.getString("itemtype");
            String prefix=datasetid;
	    /* flatten the variable namespace for the scripts to use */
            if (prefix != null){ prefix=prefix+"_"; }
            for (int i=1; i<=numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                obj.put(prefix+column_name, rs.getObject(column_name));
            }
            result.put(obj);
        }

	return result.toString();
    }
/** convert a java sql ResultSet into a json string.
 *  The DateProvider only ever gives us result sets.
 *  The Service utilities Use this to convert to string so that 
 *  they can ship results to the shell scripts
 *  shell scripts pride themselves on being unsophisticated, so we maximally flatten the json.
 *  we add a prefix to each variable name by picking out the given id column value.
 */
   public static String rs2String(ResultSet rs) throws SQLException{
        JSONArray json = new JSONArray();
        ResultSetMetaData rsmd = rs.getMetaData();
        while(rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();

            for (int i=1; i<=numColumns; i++) {
                String column_name = rsmd.getColumnName(i);
                obj.put(column_name, rs.getObject(column_name));
            }
            json.put(obj);
        }
        return json.toString(); 
    }
    


}
