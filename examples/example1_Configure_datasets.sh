#  #!/bin/sh
#  
#  The crucial component of dataflow is the dataset.  In dataflow, a dataset is represented by a single record in the dataset table.
#  This example inserts a typical dataset row.
#  
#  A dataset also carries enough information that one could use it to establish connection and browse data. Your own jobs are supplied that information when they are Launched by RunJob.  Again, DataFlow does not interpret it, but your scripts are free to use them in any way.
#  We intentionally structured it such that the field values can construct a functional jdbc connection string:
#  
#  These fields are:
#  
#  datasetid     -- The unique id of a dataset is the datasetid. 
#                   You pick whatever string is meaningful. DataFlow 
#                   does not interpret it, but uses it as an id throughout.
#  hostname      -- hostname on which the database lives
#  database      -- name of database
#  schemaname    -- schema containing the dataset
#  tablename     -- a table is the dataset 
#  username      -- the ETL user which connects to access the data
#  encryptedpass -- the password, encrypted. DataFlow never decrypts a 
#                   dataset password but will store the encrypted value 
#  
#  Suppose that your dataset is a postgres table. For concreteness we pick a table that we already have, namely the datastatus table.
#  Just so that we do not corrupt our data this will be only used as an input table. We choose the name 'datastatus' as the datasetid.
#  So (dataset,hostname,database, schemaname,tablename,username) will be ('datastatus,'localhost', 'dataflow','dataflow','datastatus', 'etl')
#  
#  We still have to put in the encrypted password. Fortunately we have an encryption utility.sh for that includes that function.
#  Let's just choose a default password, 'plugh' for testing. Never use it for production or even for system integration testing.
#  
# 

export PATH=$PATH:/path/to/utility.sh  # Put the actual path. This and RunJob are all the dataflow scripts needed.
export PASSKEY=plugh                   # If in environment, we don't have to put in on utility.sh command line
export CRYPTKEY=`utility.sh crypt -e $PASSKEY`  # we have encrypted password now 

# Now to insert the dataset
utility.sh dml "insert into dataset (datasetid,hostname,database, schemaname,tablename,username,encryptedpass) values  ('datastatus','localhost', 'dataflow','dataflow','datastatus', 'etl','$CRYPTKEY')"

# Second example. Our output is a file.  We are using the same table, but now we will shoe-horn the file descriptor into these fields.
# If that bothers you, then you can always create a view that renames the columns. But for now we just repurpose them.
# Here is the structure we find convenient.
#
# datasetid = 'datastatusExtract' # or whatever id makes it easy to recognize
# database='file:'                # just to remind us that this is a filesystem data set
# hostname='localhost'            # the file is on this server 
# schemaname='/data/dataflow'     # or wherever you put your extracts
# tablename='datastatus'          # will be a directory underneath the schema path
# username='etl'      
# encryptedpass                   # NOT SET. The etl user has file system permissions already
# 
# So here is the way to register this dataset

utility.sh dml "insert into dataset (datasetid,hostname,database, schemaname,tablename,username) values  ('datastatusextract','localhost','file:','/data/dataflow/','datastatus','etl')"

# in a later example we will show typically how these are used to contruct full path and sequenced extract files.

# Finally, lets see what we have. utility.sh datasets command will list the registered datasets. It should look like this now.

utility.sh datasets

database  datasetid          encryptedpass  hostname             schemaname       tablename   username
--------  ---------          -------------  --------             ----------       ---------   --------
dataflow  datastatus         dKpgTdzYpG...  localhost            dataflow         datastatus  etl
file:     datastatusextract                 localhost            /data/dataflow/  datastatus  etl

# Now we have two registered data sets. Next we will define a job that uses datastatus as input and a datastatusextract as output flat file. See example 2.

