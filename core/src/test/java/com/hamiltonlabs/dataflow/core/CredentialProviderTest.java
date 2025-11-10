package com.hamiltonlabs.dataflow.core;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import java.util.Properties;
import java.security.GeneralSecurityException;
import java.io.IOException;

public class CredentialProviderTest{

   public final static String TESTUSER="etl";
   public final static String TESTENC="ZpLfE+uTYE2mdmjOPrukol3yu+cpAHBnmL6trHa9PHGj";


    @Test
    void getCredentials() throws GeneralSecurityException,IOException {
	String s=CredentialProvider.getPass("plugh",TESTENC);
	assertEquals(s,"plugh");

       
	/*   This tests the getCredentials given the decryption key 
         * the "dataflow.properties" is a jdbs properties file that contains an "encrypted" field.
         * if we decrypt it using the key passed here ("plugh") it will yield the password to the dataflow database 
         *
         * the method tested here is  brazenly stubbed, just so that DataProvider can operate.
         *
	 * any call to the CredentialProvider must supply the decryption key. We use 'plugh' for test because
         * it is so silly that it is obvious you cannot use it IRL   
         */
	try{
        Properties p=CredentialProvider.getCredentials("plugh","dataflow.properties");
	assertEquals(p.get("password"),"plugh");
	}catch(Exception e){
            e.printStackTrace();
	    throw e;
        }
    }

}
