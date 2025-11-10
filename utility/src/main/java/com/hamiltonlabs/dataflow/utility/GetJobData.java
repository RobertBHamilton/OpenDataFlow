package com.hamiltonlabs.dataflow.utility;

import com.hamiltonlabs.dataflow.service.*;

public class GetJobData{

    public static void main(String[] args) throws Exception{
	
	String passkey=args[0];
	String jobid=args[1];
        String s=DataFlow.getJobData(passkey,jobid);
        System.out.println(s);
    }
    
}

