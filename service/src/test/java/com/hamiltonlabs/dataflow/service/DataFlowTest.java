package com.hamiltonlabs.dataflow.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;


import org.junit.jupiter.api.Test;

import java.security.GeneralSecurityException;
import java.sql.SQLException;
import com.hamiltonlabs.dataflow.core.*;
import java.sql.ResultSet;

@TestInstance(Lifecycle.PER_CLASS)
public class DataFlowTest{

    DataProvider p;

    @BeforeEach
    void init()throws Exception{
        p=new DataProvider().open("plugh","dataflow.properties");
	assertEquals(p.getConnection().isClosed(),false);

        String tables=DataFlow.createTables("plugh");
        assertEquals("[{\"result\":\"tables/indexes created\"}]",tables);
	
	int rows;
        String r;
	int inserts;

        System.out.printf("DataFlow Platform is %s\n",p.getPlatform());
	r=DataFlow.runUpdate(p,"delete from datastatus where dataid in ('1.0','1.1','1.2') and jobid=? ","otherjob");
	r=DataFlow.runUpdate(p,"delete from datastatus where dataid in ('1.0','1.1','1.2') and jobid=? ","loadbob");

	/* 1.0 should be skipped because it has no source already run and status is already READY */
	r=DataFlow.runUpdate(p,"insert into datastatus (dataid,datasetid,jobid,locktype,modified,status) values ('1.0','bobout','loadbob','OUT',now(),'READY')");
        assertEquals(r,"[{\"result\":\"1 rows affected\"}]");

System.out.println(DataFlow.runSql(p,"select * from dataflow.datastatus"));

	/* 1.1 should be skipped because it has already RUNNING on the IN file */
	r=DataFlow.runUpdate(p,"insert into datastatus (dataid,datasetid,jobid,locktype,modified,status) values (?,?,?,'OUT',now(),'READY')","1.1","bobin","otherjob");
         System.out.printf("inserts %s\n",r);
        assertEquals("[{\"result\":\"1 rows affected\"}]",r);

	inserts=p.runUpdate("insert into datastatus (dataid,datasetid,jobid,locktype,modified,status) values (?,?,?,'IN',now(),'RUNNING')","1.1","bobin","loadbob");
	assertEquals(inserts,1);
        System.out.printf("inserts %d\n",inserts);

	rows=p.runUpdate("delete from  job where datasetid=? and itemtype='IN' and jobid=?","bobin","loadbob");
	inserts=p.runUpdate("insert into job (datasetid,itemtype,jobid) values (?,'IN',?)","bobin","loadbob");
	assertEquals(inserts,1);
	

	/* this should be good to go because the in file is ready and no locks exist */
	inserts=p.runUpdate("insert into datastatus (dataid,datasetid,jobid,locktype,modified,status) values (?,?,?,'OUT',now(),'READY')","1.2","bobin","loadbob");
	assertEquals(inserts,1);
    }

    @Test
     void runSQL() throws SQLException {
	String s=DataFlow.runSql(p,"select user as X");
	assertEquals(s,"[{\"X\":\"ETL\"}]");
	
    }

    @Test
    void launchJobTest() throws Exception {
	String jobdata=DataFlow.launchJob("plugh","loadbob");
	System.out.printf("debug %s\n",jobdata);
	assertEquals( "[{\"dataid\":\"1.2\"}", jobdata.substring(0,17));
	String r=DataFlow.runUpdate(p, "delete from datastatus where dataid in ('1.0','1.1','1.2') and jobid in ('loadbob','otherjob')");
        assertEquals("[{\"result\":\"4 rows affected\"}]",r);
    }

    @Test
    void getJobDataTest() throws Exception {
	String jobdata=DataFlow.getJobData("loadbob",p);
	System.out.printf("debug %s\n",jobdata);
	assertEquals( "[{\"dataid\":\"1.2\"}", jobdata.substring(0,17));
	String r=DataFlow.runUpdate(p, "delete from datastatus where dataid in ('1.0','1.1','1.2') and jobid in ('loadbob','otherjob')");
        assertEquals("[{\"result\":\"4 rows affected\"}]",r);
    }
    
}
