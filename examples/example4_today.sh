#!/bin/bash


#Normally a dataflow job will not run unless all of its 'IN' data sets are ready.
#But what if your job does not have an input dataset, or if its dataset is external, meaning that it is not tracked by datasflow?
#Perhaps you just want a job to run daily, and do data checks. 
#
#We have a special dataset called 'today', which is always 'READY' and has a dataid in yyyy-mm-dd format which corresponds to today.
#If we make that dataset an 'IN' for our job, then it will run once each day, and if it succeeds it's output dataset gets the dame dataid.
#
#In this example, we have a job that checks whether there is new data in the datastatus table. The output of the job is also datastatus dataset. If there is, then job completes successfully and sets the status of (dataset=datastatus, dataid=today) to 'READY'. Thats sets it up so the extract_datastatus job can run.
#Lets call this job 'validate_datastatus'. It has an input dataset of 'today' and an output dataset of 'datastatus'. Since we already have these two datasets, we only need to add job record:
#

    utility.sh dml "insert into job (datasetid,itemtype,jobid) values ('today','IN',   'validate_datastatus')";
    utility.sh dml "insert into job (datasetid,itemtype,jobid) values ('datastatus','OUT'   ,'validate_datastatus')";
#
# Do this if you have not done it already.

    utility.sh sql "insert into dataset (datasetid,hostname,database, schemaname,tablename,username,encryptedpass) values  ('datastatus','localhost', 'dataflow','dataflow','datastatus', 'etl','ZpLfE+uTYE2mdmjOPrukol3yu+cpAHBnmL6trHa9PHGj'"

#We mark datastatus as OUT because we want the datastatus dataset to be marked as READY once our script validates it.
#This gives us the automatic variables to connect, but we don't want it to be an IN which is required to have a dataid already.  
#We can still use the connection information connect and validate.
#
#Now we can validate the data set and run the extract:

    RunJob examples/validate_datastatus.sh
    RunJob examples/extract_datastatus.sh

# The first job performs the validation and sets a dataid with READY state.
# The second discovers that the data is READY and runs the extract on it.



