package com.hamiltonlabs.dataflow.core;

import java.util.Properties;
import java.io.FileInputStream;
/** Provide credentials for all OpenDataFlow access 
 *   Credentials are user/password. They are returned in a java.util.Properties object.
 *
 *   An ironclad rule is that the passprhase used to decrypt is not available to any part of the framework.
 *   To enforce the ironclad rule, no code in core framework every determines a passphrase.
 *
 *   This way if an attacker takes complete control of the framework  database he cannot access any of the registered databases.
 *   Application is responsible for protecting the passphrase and for providing it for the framework to open a connection.
 * 
 *   The application may pass the passhphrase through command line, through environment, or obtain it from a password vault.
 *    For now we only support explicit passphrase in function call. 
 *    Environment is has significant limitations.
 *
 *
 *
 * 
 *   Passwords are encrypted and the encrypted strings may be materialized in a data store.
 *   Decryption key may be provided in environment, as an argument, or in a highly protected properties file
 */ 

public class CredentialProvider{

/** populates the credentials Properties from encrypted string and passphrase.  
    We intend that all decryption will go through this method as a security gatekeeper.

    @param passprhase  secret and highly protected passphrase to use to decrypt the password
    @param encryptedPassword the string to decrypt to get the password
    @return unencrypted password
    @throws java.security.GeneralSecurityException if the string cannot be decrypted
*/
    public static String getPass(String passphrase,String encryptedPassword) throws java.security.GeneralSecurityException {
	return AESCryptor.decrypt(encryptedPassword,passphrase);
    }

 
    /** get credentials from properties file.
     * @param passphrase the secret pass phrase to use to decrypt the password
     * @param filepath the file name of the properties file holding url,user,path
     * this properties should have entries for URL, user, encrypted,schema
     * other properties are allowed but they will be passed as parameters  to the JDBC 
     */
    public static Properties getCredentials(String passphrase,String filepath) throws java.security.GeneralSecurityException,java.io.IOException {
        Properties props=new Properties();
        props.load(new FileInputStream(filepath));
        return updateDecrypted(passphrase,props);
    }

    /** add decrypted string to the properties.
     *  @param properties the original properties object
     *  @param passphrase string containing passphrase
     *  @return the updated properties
     * If an encrypted string is in properties with name "encrypted" and it can be decrypted with passphrase,
     * then the decrypted string is added to props with key "password"
     * @throws GeneralSecurityException if string cannot be decrypted
     */
    static Properties updateDecrypted(String passphrase, Properties props)throws java.security.GeneralSecurityException{
    	props.put("password",getPass(passphrase,props.getProperty("encrypted")));
	return props;
    }
    /** get credentials.
      * @param passphrase secret passphrase to use to decrypt the password 
      * @return properties suitable for establishing jdbc connection via ConnectionManager.getConnection
      * this function handles encryption/decryption of password
      * This is totally stubbed for testing other classes and will be replaced. 
      * 
      */
}
