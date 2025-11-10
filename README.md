# OpenDataFlow

## Overview
DataFlow is a modest utility which has only one purpose: to assist in orchestration of batch job cycles which operate on partitioned or time-sliced data.
It is designed to scale in any of four dimensions:  large number of different jobs, concurrency of jobs, long dependency chains of jobs, and large number of partitions.
It does this by keeping track of the status of every data partition, reporting  on status when needed, and using the status to determine a partition of data sets which is ready to be consumed by a job.   Our one purpose is to associate that data to a particular job run and provide that info at runtime.


## Description
To use it with your executable, say ```myETL.sh```  use 

```RunJob myETL.sh```.  

Then ```RunJob``` will invoke your script after first setting some environment variables that provide connection to your datasets.  Then it captures the exit status of your script and uses it to update the status of the output data/partition so that downstream jobs know that it is now available to be consumed.

In the use cases below, there is a very remarkable feature.  **All three execute exactly the same code, and exactly the same command line**.  
This means that large migration becomes recovery and catchup becomes happy path without touching the code, without manual cleanup, without special scheduling, and completely automatically.

This pays enormous benefits for operational support, fast and reliable recovery, and tedious data migrations. 

To use it with your executable, say ```myETL.sh```  use ```RunJob myETL.sh```.  Then ```RunJob``` will invoke your script after first setting some environment variables that provide connection to your datasets.  Then it captures the exit status of your script and uses it to update the status of the output data/partition so that downstream jobs know that it is now available to be consumed.

### Here are three use cases

* **The happy path**:  Suppose that you have a script called ```myETL.sh``` and you want it to run once a day but only when the input data is ready. You kick off ```myETL.sh``` any time you like, using ```RunJob myETL.sh```.  If the data is not ready yet then it quits. If the job happens to running already or if it has already run successfully then it doesn't run again. Otherwise it runs and sets the output status to ready.  

* **Recover from large scale outage**:  Suppose you have had a platform outage and are several time cycles behind. To catch up you just do the ```RunJob myETL.sh``` repeatedly and each time you run it then it will give you a different partition automatically until you are caught up.  If you have other jobs that depend on ```myETL.sh``` you just run them repeatedly and they will take the data partitions when they are finally ready.  

* **Very large data migration to do**: Suppose it is too large to run all at once.  But the data has some date column such as modified_date and is well distributed on it.  You decide to break up the migration into batches corresponding to a calendar day values of modified_date.  Now you have thousands of jobs to run. You just add a "ready" status on the input dataset for each job, and run your migration script knowing that each time you get a different partition, guaranteed no skips and no duplication.  In fact if you are impatient you can run multiple streams concurrently, each one periodically performing exactly the same invocation: ```RunJob myETL.sh```
The dataflow utility handles all the orchestration logic.


## Getting Started

This project requires 
* Linux operating system is prefered. It has only been tested on Ubuntu.
* bash, since the RunJob and utility.sh are both bash scripts
* maven to build the project
* jq which is a command line json processor. https://jqlang.org/  Install in ubuntu with ``` sudo apt install -y jq```
* postgres   postgresql-42.7.3.jar for jdbc driver https://jdbc.postgresql.org/   You can install in ubuntu with ```sudo apt install libpostgresql-jdbc-java```
* psql, postgres client. Not required for the project but you will need it to initialize the database  ```sudo apt install postgresql-client```
* json-java  json-20250517.jar https://github.com/stleary/JSON-java  
* A functional postgres database. You will need to have a database named dataflow, schema dataflow and user named etl.

After your first mvn compile you can also snag the two jar files from your .m2 directory. They need to be on the CLASSPATH at runtime.
I think the easiest way to satisfy the postgres requirement is to install podman on your system and then start it up with something like:
````
podman run -p 5432:5432 --name pg -e POSTGRES_PASSWORD=secretpass -d docker.io/postgres
````
That gives you postgres running in your localhost and you can connect to it with
```` 
/usr/bin/psql -U etl  -d dataflow -h localhost
````
Then you can create database/schema/user and the initial tables.
See the files  docs/datamodel.txt and docs/create_tables.sql for details 

After you start the database you will need to encrypt your password (hopefully you have changed it from 'secretpass'). 
You can encrypt the password using the supplied Cryptor class.  Build the package without tests the first time to get the Cryptor class.
Encrypt with ```java -cp app/target/app-1.0.0.jar com.hamiltonlabs.dataflow.utility.Cryptor -e mysecretkey "secretpass"```
The create a file called dataflow.properties, and replace the encrypted property value with your newly enctrypted password

Contents of dataflow.properties
```` 
url=jdbc:postgresql://localhost:5432/dataflow
user=etl
schema=dataflow
encrypted=ZpLfE+uTYE2mdmjOPrukol3yuzcpAHBnxL6trHa9PHGj
````

Protect the 'mysecretkey'! Don't put it in a web document like I just did :).

The only place the password to the dataflow database is saved is in encrypted form in this properties file. Only somebody who posesses the encryption key 'mysecretkey' can decrypt it and gain access to the database.  This encryption key is not saved anywhere and can only be supplied in the command line (or in a special environment variable). 
 




