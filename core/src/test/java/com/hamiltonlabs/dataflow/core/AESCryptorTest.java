package com.hamiltonlabs.dataflow.core;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import java.util.Properties;
import java.security.GeneralSecurityException;
import java.io.IOException;

public class AESCryptorTest{

   public final static String PLAINTEXT="etlpassword";
   public final static String TESTPASS="plugh";
   public final static String TESTENCR="rENZ73c9zohlHClWbFJO5lSj0PFtdob5th7SCtY3ERA6DLdtOMQZ";


    @Test
    void encryptDecrypt()throws Exception {

	try{
            String encrypted = AESCryptor.encrypt(PLAINTEXT, TESTPASS);
            String decrypted = AESCryptor.decrypt(TESTENCR, TESTPASS);

	    /* at least make sure it is not plaintext any more */
	    assertNotEquals(PLAINTEXT,encrypted);

            /* if decrypted correctly then cryptor is working */
	    assertEquals(PLAINTEXT,decrypted);
	}catch(Exception e){
            e.printStackTrace();
	    throw e;
        }
    }

}
