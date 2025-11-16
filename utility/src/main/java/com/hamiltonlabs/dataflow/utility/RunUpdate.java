package com.hamiltonlabs.dataflow.utility;

import com.hamiltonlabs.dataflow.service.*;
import java.sql.SQLException;

public class RunUpdate{


    public static String run(String passkey,String sqltext){
	   return DataFlow.runUpdate(passkey,sqltext);
    }
    
    public static void main(String[] args) throws Exception{
	System.out.println(run(args[0],args[1]));
    }
}

