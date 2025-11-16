package com.hamiltonlabs.dataflow.utility;

import com.hamiltonlabs.dataflow.core.*;
import com.hamiltonlabs.dataflow.service.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class GetJobRuns{

    static String sqltext="select * from  datastatus where locktype='OUT' order by modified desc limit 20";

    public static String run(String passkey) throws Exception{	
	return DataFlow.runSql(passkey,sqltext);
//        DataProvider p=new DataProvider().open(passkey,"dataflow.properties");
//	return DataFlow.rs2String(p.runSQL(sqltext));
    }
    public static void main(String[] args) throws Exception{
	System.out.println(run(args[0]));
    }
    
}

