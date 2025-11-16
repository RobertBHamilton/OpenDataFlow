package com.hamiltonlabs.dataflow.utility;

import com.hamiltonlabs.dataflow.service.*;

public class SetJobEndStatus{

    public static String run(String passkey,String jobid,String dataid,String status) throws Exception{
        return DataFlow.setJobEndStatus(passkey,jobid,dataid,status);
    }

    public static void main(String[] args) throws Exception{
	System.out.println(run(args[0],args[1],args[2],args[3]))	;
    }
    
}

