package com.hamiltonlabs.dataflow.utility;

import com.hamiltonlabs.dataflow.core.*;
import java.io.IOException;
import java.security.GeneralSecurityException;

/* fixme: we need a wrapper of this in the service module */
public class Cryptor{

    public static String run(String key,String cryptswitch,String txt)throws Exception{

        switch (cryptswitch){
	    case "-e":
                return AESCryptor.encrypt(txt, key);
	    case "-d":
                return AESCryptor.decrypt(txt,key);
	    default:
                return  help();
        }
    }	
    public static String help(){
        return "Encrypt: java com.hamiltonlabs.dataflow.utilityCryptor -e key text \nDecrypt: java com.hamiltonlabs.dataflow.Cryptor -d key text ";
    }

    /* called with args [-e|-d] key text. But all dataflow methods must have passkey first so we reorder */
    public static void main(String[] args) throws Exception{
            System.out.println(run(args[1],args[0],args[2]));
    }
}
