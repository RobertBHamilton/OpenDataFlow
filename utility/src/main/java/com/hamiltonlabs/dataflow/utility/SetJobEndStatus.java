package com.hamiltonlabs.dataflow.utility;

import com.hamiltonlabs.dataflow.service.*;

public class SetJobEndStatus{

    public static void main(String[] args) throws Exception{
	
	String passkey=args[0];
	String jobid=args[1];
	String dataid=args[2];
	String status=args[3];
        String s=DataFlow.setJobEndStatus(passkey,jobid,dataid,status);
        System.out.println(s);
    }
    
}

