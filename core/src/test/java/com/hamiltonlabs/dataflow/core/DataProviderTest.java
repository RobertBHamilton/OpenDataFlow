package com.hamiltonlabs.dataflow.core;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.sql.ResultSet;

@TestInstance(Lifecycle.PER_CLASS)
public class DataProviderTest{

    DataProvider p;

    /* this relies on a stub in the credentialProvider, which should be mocked here instead 
     *  it relies also on local postgress database listening for connections. Mock or use H2
     * To get credentials we need the dataflow.properties file which contains an encrypted field,
     * and the encryption key ("plugh"). The open() will pass these to the CredentialProvider and
     * if it successfully decrypts the password to the dataflow database then the open will succeed
     * We test this by checking that the connection is indeed open.  
     * in addetion we run a couple of SQL to verify the operation
     */
    @Test
    @BeforeAll
    void init()throws Exception{
	p=new DataProvider().open("plugh","dataflow.properties");
	assertEquals(p.getConnection().isClosed(),false);
    }

    /* run some sql and get expected result */
    @Test
    void runUpdate() throws SQLException {

	int rs=p.runUpdate("create table bob(x int)");
	assertEquals(rs,0);

	/* tests both table was created and inster is working */
	rs=p.runUpdate("insert into bob values(1)");
	assertEquals(rs,1);

        /* cleanup */
	rs=p.runUpdate("drop table bob");
	
    }
    /* print out the value of lockStatusSQL should be empty string if h2 */
    @Test
    void getResources(){
	String lock=p.getSQL("lockStatusSQL");
	String platform=p.getPlatform();
	if (platform.equals("h2")){
	    assertEquals("",lock);
	} 
	if (platform.equals("postgres")){
	    assertEquals("lock datastatus in access exclusive mod",p);
	}
    } 
    /* run some sql and get expected result */
    @Test
    void runSQL() throws SQLException {
	ResultSet rs=p.runSQL("select 1 as one where ?='1'","1");
	rs.next();
	assertEquals(rs.getString("one"),"1");
	
    }
    
}
