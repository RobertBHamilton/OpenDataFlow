# OpenDataFlow

## Overview

OpenDataFlow is a lightweight orchestration utility that runs and coordinates batch jobs over partitioned or time-sliced data so teams can schedule, recover, and migrate large data-processing pipelines without changing their ETL code.

This quickstart uses H2 for simplicity.

It has been tested on Ubuntu and runs in a bash shell. 

1.  Requirements to run the demo:

- bash and the standard command line utilities
- jq (`sudo apt install -y jq`)
- The DataFlow jar: dataflow-1.0.0.jar
- the decryption passkey in $PASSKEY environment variable
  `  export PASSKEY=plugh `
- The two supplied scripts: `utility.sh` and `RunJob`

Have jq on the your path, and put the dataflow-1.0.0.jar in the same directory as the scripts.

2. Set up the h2 database, schema and tables:

```  ./utility.sh createtables```

   The initial connection to H2 creates the database, schema, and user automatically
   The createtables utility creates the standard dataflow tables in the database.

3. Configure the 'loadbob' job and the datasets that it uses 
```
   ./utility.sh dml "insert into dataset (datasetid) values  ('bobin')"
   ./utility.sh dml "insert into dataset (datasetid) values  ('bobout')"
   ./utility.sh dml "insert into job (datasetid,itemtype,jobid) values ('bobout','OUT','loadbob')"
   ./utility.sh dml "insert into job (datasetid,itemtype,jobid) values ('bobin' ,'IN', 'loadbob')"
```
These insert test data into the schema that are enough to simulate a run.

The first two commands register two datasets named 'bobin' and 'bobout'.  
The second two commands associates bobin and bobout as input and output data sets respectively with the job named 'loadbob'
These inserts should only happen when one time to configure the job and datasets.

4. Set a status for the input dataset 

 ```  
./utility.sh dml "insert into datastatus (dataid,datasetid,jobid,locktype,modified,status) values ('1.0','bobin','fakejob', 'OUT',now(),'READY')"
```


We give it a fake dataid, and specify a fakejob that "produced" it. The status of READY for  an OUT data chunk means that it is ready and safe to bebe consumed.

5. "Write" a `loadbob.sh` to run. In this example it is just a one-liner that outputs some of the automatic environment variables.

```  
  echo 'echo "running loadbob with dataid $dataid partition of input  $bobin_DATASETID"' > loadbob.sh && chmod +x loadbob.sh 
```
  
  One important note: the jobid is **inferred** from the name of the script. That means that if our jobid is 'loadbob' then the script has to be named 'oadbob.sh'. This is mandatory, but is just the way that the RunJob script is written. The intent is to keep it simple so that the only parameter to RunJob is the script name.

6. Run the job with RunJob

```    
    RunJob ./loadbob.sh 
```

Output should look like this:

```text
  Mon Dec  1 04:08:50 PM CST 2025: Launching ./loadbob.sh with dataid 1.0
  running loadbob with dataid 1.0 partition of input  bobin
  Mon Dec  1 04:08:50 PM CST 2025: Job ./loadbob.sh is complete. Updating status
  1 rows updated to READY for loadbob and 1.0 1 IN file-local locks released
```
Two log-style messages, confirming the start and end of the loadbob job, and the one line output by the `loadbob.sh` script
The last line informational message indicating that DataFlow has set the final status


7. Checks:
   do  `RunJob ./loadbob.sh` a second time, and confirm that it will refuse to do a duplicate run.
   check the data with utility:

```text
 ./utility.sh runs
    DATAID  DATASETID  JOBID    LOCKTYPE  MODIFIED                    STATUS
    ------  ---------  -----    --------  --------                    ------
    1.0     bobout     loadbob  OUT       2025-12-01 16:08:49.740813  READY
    1.0     bobin      fakejob  OUT       2025-12-01 15:46:19.56124   READY
```

   Check the data with direct SQL's:

```text
   utility.sh sql "select * from datastatus"
DATAID  DATASETID  JOBID    LOCKTYPE  MODIFIED                    STATUS
------  ---------  -----    --------  --------                    ------
1.0     bobin      fakejob  OUT       2025-12-01 15:46:19.56124   READY
1.0     bobout     loadbob  OUT       2025-12-01 16:08:49.740813  READY

``` 


## Remarks

* We started with just the jar file and had to manually create the schema and tables. But if you build the package with maven, the tests will build the H2 database, schema, tables, user and password for you,  and the dataflow-1.0.0.jar will be in utilities/target/dataflow-1.0.0.jar

* Access to the h2 database for testing is through user ETL and password which was encrypted using the default passkey 'plugh'. You should encrypt your own password using your own passkey and put it into the dataflow.properties as soon as possible.  
The encrypted password and other connection information is in core/src/main/resources/dataflow.properties. You can copy it to your working directory and modify it, and the utilities will override the core/ properties file if they find this one. The encryption is easily done because it is one of the functions published by the utilities.sh tool.


* In normal day-to-day operation you **never** need to update or insert the datastatus table, not in your code, not manually the way we did in this example. RunJob handles that for you.  The inserts to job and dataset tables are one-time things to register and confure the datasets or to set up a test case.  
In exceptional cases such as handling errors, you encounter a job in FAILED state. If in that case you want the job to run again, you can reset the job to RESUBMIT.  You can either do a dml command, like `utility.sh dml 'update datastatus set status to RESUBMIT where jobid='loadbob' and dataid='1.0'` though I would just endjob utility command:
```
	utility.sh endjob loadbob 1.0 RESUBMIT
```  
The big advantage is that you don't have to ask the scheduling team to make any changes, and you don't have to worry about command line parameters because there are not any.  If the job is scheduled to run multiple times a day, then it will just catch up the next time it runs, and there are no changes in production at all except for the RESUBMIT status.  That means you avoid an enormous amount of red tape and committee meetings just to rerun the job.


 *  The actual dataset record has fields for things like hostname, database name, schema, table, username and (***encrypted***) password. These all appear as automatic variables to your script. This avoids all issues related to the temptation of hardcoding this metadata, the headaches involved with maintaining it, possible errors in connection strings, and having to make changes when moving from development to production.  
It is possible, and in our opinion best practice to ***hard-code nothing in your script***.  Get it all from the metadata that DataFlow provides.  For one thing if you have one job producing data and another job consuming it, you are now using named datasets and so both jobs are guaranteed to be using the same dataset. Almost no chance of second job picking up the wrong data because of a misconfiguration.
Not only that, the framework guarantees that the second job will not have any false starts while the first job is running or in any error state. 

* The dataset metadata are not restricted to only jdbc connections. They can be repurposed to file system paths, web page urls, tcp endpoints, what have you. The semantics is entirely up to the consumer (the ETL script) which can do whatever they want with it. DataFlow doesn't use it at all.

* Some of the other examples in the examples directory illustrate these points.


