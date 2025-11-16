package com.hamiltonlabs.dataflow.utility;

import com.hamiltonlabs.dataflow.service.*;


public class GetJobData{

    public static String run(String passkey,String jobid)throws Exception{
        return DataFlow.getJobData(passkey,jobid);
    }

    public static void main(String[] args) throws Exception{
        System.out.println(run(args[0],args[1]));
    }
    
}

