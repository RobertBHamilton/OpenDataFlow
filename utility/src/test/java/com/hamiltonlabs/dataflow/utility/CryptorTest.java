package com.hamiltonlabs.dataflow.utility;

import com.hamiltonlabs.dataflow.core.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/* Test of the utility Cryptor.
   It should accept command line and perform encrypt and decrypt and emit result onto std out
   com.hamiltonlabs.dataflow.utility.Cryptor -d plugh "szPoK2EiZ6Go9MwB8iR5xox467r2xmMEr8UATyPOMwDoUw=="
   in test we will call the main() directly and capture the text output
*/

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CryptorTest{

   public final static String PLAINTEXT="etlpassword";
   public final static String TESTPASS="plugh";
   public String encrypted;

   private ByteArrayOutputStream outContent; 
    private final PrintStream originalOut = System.out;

    @BeforeEach 
    public void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    @Order(3)
    void testHelpCryptor()throws Exception {
            String s=Cryptor.help();
	    assertNotEquals("",s);
    }	
    
    @Test
    @Order(1)
    void testMainCryptorE()throws Exception {

	try{
	    restoreStreams();

            encrypted=Cryptor.run(TESTPASS,"-e",PLAINTEXT);
	    assertNotEquals(PLAINTEXT,encrypted);

	}catch(Exception e){
            System.out.println("Caught exception in encrypt test");	
            e.printStackTrace();
	    throw e;
        }
    }

    @Test
    @Order(2)
    void testMainCryptorD()throws Exception {

	try{

	    restoreStreams();
            String decrypted=Cryptor.run(TESTPASS,"-d",encrypted);
	    assertEquals(PLAINTEXT,decrypted);

	}catch(Exception e){
            e.printStackTrace();
	    throw e;
        }
    }


}

