package com.hamiltonlabs.dataflow.service;

import com.hamiltonlabs.dataflow.core.*;
import java.util.List;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;


/** DataFlow exoses all the serviced needed to support the full job lifecycle.
  * getJobData provides all the runtime configuration needed for the data sets used by the job
  * startJob gets global rw lock on the output dataset and job-local locks on all input sets
  * endJob sets final status on output dataset and releases job-local locks 
  * All lock operations are built on setStatus which upserts rows into datastatus table.
  */

public class DataFlow {
  
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
	
	String updateSQL="update datastatus set status=?,locktype=? where dataid=? and datasetid=? and jobid=?";
        String insertSQL="insert into datastatus values (?,?,?,?,?,now())";
	int updatecount=dataprovider.runUpdate(updateSQL,status,locktype,dataid,datasetid,jobid);
	if (updatecount==0){
	    updatecount=dataprovider.runUpdate(insertSQL,datasetid,jobid,dataid,locktype,status);
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
	String updateOutStatusSQL="update datastatus set status=? where jobid=? and dataid=? and locktype='OUT'	";
	String updateFileLocalStatusSQL="delete from datastatus where jobid=? and dataid=? and locktype='IN'	";

	int updatecount=dataprovider.runUpdate(updateOutStatusSQL,status,jobid,dataid);
        int deletecount=dataprovider.runUpdate(updateFileLocalStatusSQL,jobid,dataid);

	 returnString=String.format("%d rows updated to %s for %s and %s",updatecount,status,jobid,dataid);
         returnString=returnString+String.format(" %d IN file-local locks released",deletecount);

	return returnString;
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
     */
    public static String getJobData(String jobid,String passkey) throws Exception{
         DataProvider dataprovider=new DataProvider().open(passkey,"dataflow.properties");

         /*  First query gets a dataid for the data. If there is one, then the second query gets every data set configuration.  
          *  Then we get any additional env items that were registered for the job.
          *  We munge it all together into a json object  and return it
          * TODO: Migrate sql to a resource 
          */

	 String dataidSQL="select x.dataid,x.jobid from  (select j.datasetid,j.jobid,d.dataid  from job j left join datastatus d on j.datasetid=d.datasetid and d.locktype='OUT' and d.status='READY' where j.itemtype='IN' and j.jobid=?)x where not exists (select d.dataid from job j join datastatus d on j.jobid=x.jobid  and j.itemtype='IN' and d.locktype='IN' and j.datasetid=d.datasetid and d.dataid=x.dataid) and not exists (select d.dataid from job j join datastatus d on j.jobid=x.jobid and j.itemtype='OUT' and d.datasetid=j.datasetid and d.dataid=x.dataid where d.status != 'RESUBMIT')  order by x.dataid limit 1;" ;
        String datasetSQL="select d.*,itemtype from dataset d join job on job.datasetid=d.datasetid where job.jobid=?";

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
	/* If we get  here we have found a dataset. Time to lock the tables as we go */
    //setStatus(String datasetid,String jobid,String dataid,DataProvider dataprovider,String locktype,String status) 
        
	rs=dataprovider.runSQL(datasetSQL,jobid);
       ResultSetMetaData rsmd = rs.getMetaData();
        while(rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();
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
