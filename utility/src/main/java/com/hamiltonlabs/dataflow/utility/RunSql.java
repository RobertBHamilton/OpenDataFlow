package com.hamiltonlabs.dataflow.utility;

import com.hamiltonlabs.dataflow.core.*;

public class RunSql{

    public static void main(String[] args) throws Exception{
	
	String passkey=args[0];
	String sqltext=args[1];
        DataProvider p=new DataProvider().open(passkey,"dataflow.properties");
	System.out.println(p.runSQL(sqltext));
    }
    
}

