package com.hamiltonlabs.dataflow.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.Test;

import java.security.GeneralSecurityException;
import java.sql.SQLException;
import com.hamiltonlabs.dataflow.core.*;

public class DataFlowTest{

    /* assumes that dataflow has been configured with test data and creds were encrypted with the test key "plugh" */
    @Test
    void getJobDataTest() throws Exception {
	assertEquals( "[{\"dataid\":\"1.2\"},", DataFlow.getJobData("loadbob","plugh").substring(0,18));
	//assertEquals( "[{\"dataid\":\"1.2\"},", DataFlow.getJobData("loadbob","plugh"));
    }
    
}
