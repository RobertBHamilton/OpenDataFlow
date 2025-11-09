package com.hamiltonlabs.dataflow.utility;

import com.hamiltonlabs.dataflow.core.*;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class Cryptor{
    public static void help(){
	   System.out.println("To encrypt: java com.hamiltonlabs.dataflow.Cryptor -e key text \nto decrypt: java com.hamiltonlabs.dataflow.Cryptor -d key text");
    }

    public static void main(String[] args)throws Exception{
	if (args.length<1){
	   help();
        } else {

		String cryptswitch=args[0];
		String key=args[1];
		String txt=args[2];
		try {
	   	    if (cryptswitch.equals("-e")){
            		String encrypted = AESCryptor.encrypt(txt, key);
			System.out.print(encrypted);
		    } else {
		        if (cryptswitch.equals("-d")){
                            String decrypted = AESCryptor.decrypt(txt,key);
			    System.out.print(decrypted);
		        } else {
		  	    help();
    		        }
		   }	
	        }catch(Exception e){
            		e.printStackTrace();
		        throw e;
                }
	}
    }
}
