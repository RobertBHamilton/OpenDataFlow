
/* job contains all static configuration for the job
 * It should have a row for every input or output dataset the job uses 
 * and a row with env item that we wish to provide for the job. 
 */
drop table if exists dataflow.job;
create table job(
    jobid varchar,
    itemtype varchar,    /* for data, IN or OUT, or name of env variable  */
    datasetid varchar,   /* null for environment item   */
    itemvalue varchar,    /* null if IN or OUT item      */
    modified  timestamp
);

/*  Contain sufficient information for ETL script to establish a connection,
 *  provided that he can decrypt the password:
 *  database,schema,table,user,encrypted password 
 *  Yes these are db centric in a generic table but they can be mapped to 
 *  other types of data sets like files, web urls, hdfs tables etc.
 */

drop table if exists dataflow.dataset;

create table dataflow.dataset(
    datasetid varchar,
    hostname varchar,
    database varchar,
    schemaname varchar,
    tablename varchar,
    username varchar,
    encryptedpass varchar
); 
create index x_dataset on dataflow.dataset(datasetid);

drop table if exists dataflow.datastatus;

create table dataflow.datastatus(
    datasetid varchar,
    jobid varchar,
    dataid varchar,
    locktype varchar,
    status varchar,
    modified timestamp,
primary key (datasetid,jobid,dataid)
);
