package com.hamiltonlabs.dataflow.utility;

import com.hamiltonlabs.dataflow.core.*;
import com.hamiltonlabs.dataflow.service.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class RunSql{

    public static void main(String[] args) throws Exception{
	
	String passkey=args[0];
	String sqltext=args[1];
        DataProvider p=new DataProvider().open(passkey,"dataflow.properties");
	System.out.println(DataFlow.rs2String(p.runSQL(sqltext)));
    }
    
}

