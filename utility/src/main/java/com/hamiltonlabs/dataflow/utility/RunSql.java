package com.hamiltonlabs.dataflow.utility;

import com.hamiltonlabs.dataflow.core.*;
import com.hamiltonlabs.dataflow.service.*;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class RunSql{

    public static String run(String passkey,String sqltext){	
	return DataFlow.runSql(passkey,sqltext);
    }
    public static void main(String[] args) throws Exception{
	System.out.println(run(args[0],args[1]));
    }
    
}

