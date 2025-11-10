package com.hamiltonlabs.dataflow.core;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;



/** provide multi platform functionality for jdbc connections
 * sets and keeps schema,user,platform 
 * provides a simplified SQL execution that is used by DataFlow:
 *    runSQL returning a List of String records for multicolumn multirow queries
 *    (todo) runSQL returning a record of strings for multicolumn queries 
 *
 * DataFlow and DataSet will extend this class and add their own functions to implement their parts of the dataflow model.
 * Uses CredentialsProvider to securely opens connections.
 */ 

public class DataProvider implements AutoCloseable{

    /* used to open a connection to the database */
    private static final Properties properties = new Properties();

    /* the jdbc connection used by this instance.  */
    private Connection connection;

    /** get the connection used by this data provider.
     *  @return the connection object
     */
    public Connection getConnection(){
        return connection;
    } 


    /** open the connection and set default search path.
     * @param passphrase String used as key to decrypt the dataflow password
     * @param props  filesystem path to the properties file
     * @return this DataProvider object with an open connection
     */ 
    public DataProvider open(String passphrase,String props)throws SQLException,java.security.GeneralSecurityException,java.io.IOException{
        Properties creds=CredentialProvider.getCredentials(passphrase,props);
	String url=creds.getProperty("url");
	connection=DriverManager.getConnection(url,creds);
	connection.setSchema(creds.getProperty("schema"));
	return this;
    }	


    /** submit arbitrary DML SQL with bind variables 
     * @param SQL sql to run
     * @param vars zero or more strings to bind to the statement
     * @return Json in string
     */
    public int runUpdate(String ... vars) throws SQLException{
	try{
            String sql=vars[0];
	    PreparedStatement st=connection.prepareStatement(sql);	
            for (int i=1;i<vars.length;i++){
		st.setString(i,vars[i]);
	    }
	    int rs=st.executeUpdate();
	    return rs;
        } catch(SQLException e){
	    e.printStackTrace();
            throw new SQLException(e);
        }
    }

    /** submit arbitrary SQL with bind variables 
     * @param SQL sql to run
     * @param vars zero or more strings to bind to the statement
     * @return Json in string
     */
    public ResultSet runSQL(String ... vars) throws SQLException{
	try{
            String sql=vars[0];
	    PreparedStatement st=connection.prepareStatement(sql);	
            for (int i=1;i<vars.length;i++){
		st.setString(i,vars[i]);
	    }
	    ResultSet rs=st.executeQuery();
	    return rs;
        } catch(SQLException e){
	    e.printStackTrace();
            throw new SQLException(e);
        }
    }


    /** Just runs an arbitrary SQL with no bind variables 
     * @param SQL sql to run
     * @return List of records comprising the result
     * Deprecated
     */
    public List<String> runSQL_List(String sql) throws SQLException{
	ArrayList<String> results=new ArrayList<String>();
	try(
	    PreparedStatement st=connection.prepareStatement(sql);	
	    ResultSet rs=st.executeQuery();){
	    while (rs.next()){
	        results.add(rowToString(rs));
	    }
        } catch(SQLException e){
	    e.printStackTrace();
            throw new SQLException(e);
        }
        return results;
    }

    /* Converts a row in a result set to a comma delimitted string
     *  we must get the column types (blobs need special treatment, timestamps also need formatting).
     * This is _under construction_. For now we will handle the easy data types
     */
    public String rowToString(ResultSet rs) throws SQLException{ 
	ResultSetMetaData meta=rs.getMetaData();
	int n=meta.getColumnCount();
	StringBuilder rowString=new StringBuilder();
	rowString.append(rs.getString(1));
	for (int i=2;i<=n;i++){
	    rowString.append(","+rs.getString(i));
	}
	return rowString.toString();
     }
    /** closes the connection.
     * @throws SQLConnection if the close operation fails
     */
    public void close() throws SQLException{
	connection.close();
   }

}
