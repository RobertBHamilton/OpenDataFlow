package com.hamiltonlabs.dataflow.utility;

import com.hamiltonlabs.dataflow.service.*;

/* upsert a data status row for given dataid,datasetid,jobid,locktype */
public class SetDataStatus{

    static String updateSQL="update datastatus set status=?,modified=now() where dataid=? and datasetid=? and jobid=? and locktype=?";
    static String insertSQL="insert into datastatus (dataid,datasetid,jobid,locktype,modified,status) values (?,?,?,?,now(),?)";


    public static String run(String passkey,String status,String dataid,String datasetid,String jobid,String locktype)
    throws Exception{

        int rows=DataFlow.runUpdate(passkey,updateSQL,status,dataid,datasetid,jobid,locktype);
        if (rows==0){
            rows=DataFlow.runUpdate(passkey,insertSQL,dataid,datasetid,jobid,locktype,status);
        }
	return String.format("[{\"message\":\"%d rows affected\"}]",rows);
    }
    
    public static void main(String[] args) throws Exception{
	System.out.println(run(	args[0],args[1],args[2],args[3],args[4],args[5]));
    }
}

