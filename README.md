# OpenDataFlow

## Overview
OpenDataFlow is a lightweight orchestration utility that runs and coordinates batch jobs over partitioned or time-sliced data so teams can schedule, recover, and migrate large data-processing pipelines without changing their ETL code.
It does this by keeping track of the status of every data partition, reporting  on status when needed, and using the status to determine a partition of data sets which is ready to be consumed by a job.   Our one purpose is to associate that data to a particular job run and provide that info at runtime.

### Why this exists
Many data teams spend disproportionate time and engineering effort on the same operational problems: recovering after platform outages, catching up missed cycles, and migrating huge datasets in stages. OpenDataFlow was born out of repeated large migrations and outages. It encodes the orchestration and state-tracking so recovery, catch-up, and phased migration are first-class, routine operations — using the same job scripts you already have.


### Key use cases (read these first)
- Happy path — reliable, periodic runs
  Run your existing ETL script any time; OpenDataFlow only processes partitions that are ready. No scheduler changes, no code changes.
- Catch-up after outage — automated recovery
  If the platform goes down or you fall behind, rerun the same command repeatedly: OpenDataFlow will pick up the next unprocessed partition each time until you’re caught up.
- Large, phased migrations — incremental, safe migration
  Migrate large tables by splitting on a date or shard column and repeatedly running the identical job across partitions. No special migration code, no manual state cleanup.

### Why this approach helps
- Your existing scripts can run unchanged through RunJob.
- Per-partition state is persisted so retries and dependencies are handled correctly.
- Operational tasks (recovery, catch-up, phased migration) become identical to normal runs — reducing human error and support time.

Environment: how your job receives context
Before invoking your script, RunJob provisions the process environment with automatic variables that describe the partition and the dataset to process (for example: a partition id, connection/metadata descriptors, and credentials decrypted at runtime). These variables are the intended integration surface for your existing ETL scripts so you generally do not need to change your job code. 

See the examples/ directory for exact variable names and sample scripts that read them.

Security note: the decrypted credential is provided only at runtime in the job process’s environment. Be careful not to echo or persist it in logs. Keep the encryption key used to create the encrypted password secret.

### Quick start (5 minutes)
1. Prerequisites
   - Linux (Ubuntu tested), bash, maven
   - jq (sudo apt install -y jq)
   - PostgreSQL (or run a local container)
   - json-java (json-20250517.jar) and the Postgres JDBC driver (e.g., postgresql-42.7.3.jar)
   - A Postgres database named `dataflow`, schema `dataflow`, and a user `etl`

2. Run a local Postgres (optional)
   podman run -p 5432:5432 --name pg -e POSTGRES_PASSWORD=secretpass -d docker.io/postgres

3. Build
   mvn package

4. Initialize database
   Connect with psql and run docs/create_tables.sql. See docs/datamodel.txt for schema notes.

5. Configure
   Encrypt the DB password with the included Cryptor class:
   java -cp app/target/app-1.0.0.jar com.hamiltonlabs.dataflow.utility.Cryptor -e <key> "<password>"
   Create the file **dataflow.properties** and place the url,user,schema, and encrypted fields. This tells the utility how to access the dataflow database.
   
   ```
   url=jdbc:postgresql://localhost:5432/dataflow
   user=etl
   schema=dataflow
   encrypted=<encrypted-password-here>
   ```
   Keep the encryption key private.

7. Run your job
   Make your ETL script executable (e.g., myETL.sh) and invoke it via:
   RunJob <key> <jobid for myetl> myETL.sh
   
   RunJob exports the environment variables described above, runs your script, captures exit status, and records the result to the dataflow DB.
   You can choose to use any or none, though at least you need to know the partitionid (that is $dataid)
   However, the other automatic variables contain data descriptors for your data source and they are very convenient to have.
      
### Core concepts
- Partition — a tracked unit of data (date, shard, etc.)
- RunJob — the wrapper that populates environment variables, invokes your script, and records success/failure
- State machine — persisted partition states drive readiness, retries, and dependency resolution

### Files of interest
- dataflow.properties  - contains connection info to your dataflow database.
- docs/create_tables.sql — SQL to create the required tables
- docs/datamodel.txt — explanation of the data model
- examples/ — sample ETL scripts and fixtures that show how RunJob provisions environment variables (see variable names and usage)
- RunJob, utility.sh — the bash wrappers invoked in production
- app/target/...jar — built artifacts after mvn package

### Status & roadmap
- Focused on orchestration for partitioned ETL; tested on Ubuntu.
- Planned: examples/ with a minimal demo ETL, support for H2, and a containerized dev environment.

### Contributing
- Open an issue or PR. Small examples and updated docs are very welcome.

### License
-  MIT

### Maintainer
- Robert B Hamilton — RobertBHamilton/OpenDataFlow



