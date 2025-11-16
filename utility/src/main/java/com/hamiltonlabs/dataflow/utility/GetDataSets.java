package com.hamiltonlabs.dataflow.utility;

//import com.hamiltonlabs.dataflow.core.*;
import com.hamiltonlabs.dataflow.service.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class GetDataSets{

/* truncate the encrypted string if it gets too long */

    static String sqltext="select datasetid,hostname,database,schemaname,tablename,username,case when length(encryptedpass)>10 then concat(substring(encryptedpass,1,10),'...') else encryptedpass end  as encryptedpass from dataset";


    public static String run(String passkey) throws Exception{	
	return DataFlow.runSql(passkey,sqltext);
        //DataProvider p=new DataProvider().open(passkey,"dataflow.properties");
	//return DataFlow.rs2String(p.runSQL(sqltext));
    }

    public static void main(String[] args) throws Exception{
	System.out.println(run(args[0]));
    }
    
}

