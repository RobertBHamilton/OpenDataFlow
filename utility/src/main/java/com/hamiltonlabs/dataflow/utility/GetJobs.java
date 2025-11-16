package com.hamiltonlabs.dataflow.utility;

import com.hamiltonlabs.dataflow.core.*;
import com.hamiltonlabs.dataflow.service.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/** List all jobs registered in the DataFlow */
public class GetJobs{

    static String sqltext="select * from  job";

    public static String run(String passkey) throws Exception{	
	return DataFlow.runSql(passkey,sqltext);
//        DataProvider p=new DataProvider().open(passkey,"dataflow.properties");
//	return DataFlow.rs2String(p.runSQL(sqltext));
    }
    public static void main(String[] args) throws Exception{
	System.out.println(run(args[0]));
    }
    
}

