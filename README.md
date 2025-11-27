# OpenDataFlow
NOTE This application is in BETA. It still needs some work to get to first release. Contributers are welcome

## Overview
OpenDataFlow is a lightweight orchestration utility that runs and coordinates batch jobs over partitioned or time-sliced data so teams can schedule, recover, and migrate large data-processing pipelines without changing their ETL code.
It does this by keeping track of the status of every data partition, reporting  on status when needed, and using the status to determine a partition of data sets which is ready to be consumed by a job.   Our one purpose is to associate that data to a particular job run and provide that info at runtime.

### Why this exists
Many data teams spend disproportionate time and engineering effort on the same operational problems: recovering after platform outages, catching up missed cycles, and migrating huge datasets in stages. OpenDataFlow was born out of repeated large migrations and outages. It encodes the orchestration and state-tracking so recovery, catch-up, and phased migration are first-class, routine operations — using the same job scripts you already have.
## Motivation

### The Five Questions: A Transactional Manifesto for Batch ETL

Years ago, while diagnosing root causes of ETL failures, the same five questions surfaced again and again. Answering them required:

- Deep code dives  
- Runtime environment spelunking  
- Direct inspection of datasets  
- Log archaeology to infer concurrency state  

Each investigation took **20–30 minutes**.  
After a platform outage affecting 100+ jobs?  
**Hours lost. Risky shortcuts taken. Burn out.**

So we asked two natural questions:

1. **Why not capture this information *at runtime* so it’s instantly available during forensics?**  
2. **If it’s critical for root cause, isn’t it *even more* critical *before* the job runs?**

The answer to both was obvious.  
That insight birthed the **DataFlow utilities** and eventually, **OpenDataFlow**.

---

| # | Question                                 | Transactional Analogy             | What Breaks Without It                     | OpenDataFlow’s Answer                                      |
|---|------------------------------------------|-----------------------------------|--------------------------------------------|------------------------------------------------------------|
| 1 | **Is the data ready?**                   | *Commit prerequisite*             | Jobs start on partial/incomplete inputs    | `RunJob` waits on `datastatus = READY`                     |
| 2 | **Is it the *right* data?**              | *Isolation + Correctness*         | Loading yesterday’s file → silent corruption | Partition key + timestamp validation                       |
| 3 | **Is it in the right location?**         | *Environment isolation*           | Dev paths in prod → data loss or breach    | Config-driven paths, no hardcoding                         |
| 4 | **Has it been validated?**               | *Integrity check before commit*   | Garbage in → garbage forever               | Schema/row-count/sanity checks *before* lock               |
| 5 | **Is it safe to access?**                | **Concurrency control (locking)** | Race conditions, cleanup collisions, double runs | **`dataid + datastatus` with transactional semantics**     |

---

### 2-Phase Commit — Without the Ceremony

We use these questions to enforce a protocol **identical in spirit to 2-phase commit**:

| 2PC Phase           | OpenDataFlow Equivalent                            | Implementation                                                                 |
|---------------------|----------------------------------------------------|--------------------------------------------------------------------------------|
| **Phase 1: Prepare**   | `RunJob` checks **all 5 Questions**                | Scans `data_manifest`, `datastatus`, locks, paths, validation                  |
| **Yes Vote**           | All inputs = `READY`, no lock conflicts            | Every input confirms: “I’m complete, valid, and exclusively available”         |
| **No Vote / Abort**    | Any question fails                                 | Job exits early with clear code: `DATA_NOT_READY`, `LOCKED`, `INVALID_PATH`     |
| **Phase 2: Commit**    | Execute `job.sh` → write output                    | Only runs if *all* participants voted yes                                      |
| **Post-Commit**        | Mark output `COMPLETE`, release lock               | Enables safe downstream consumption                                            |

> **This is 2PC without the ceremony — and it works on RDBMS, NFS, S3, or a USB stick.**  
> *Because the transactional guarantees are enforced by the **ETL jobs themselves**, not the storage layer.*

---

### The Legend Moment: Autonomic Recovery

After a platform outage:

1. You mark failed jobs:  
   ```sql UPDATE job_status SET status = 'RESUBMIT' WHERE dataid = '2025-11-12'; ```
2. Platform comes back online.
3. Your regular scheduler runs RunJob dailyETL.sh as always.
4. All RESUBMIT jobs automatically resume — safely, correctly, in order.

***_No manual reruns. No fire drills. No heroics._*** The system heals itself.  
This is  ***self-correcting data infrastructure.***



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
   RunJob myETL.sh
   
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



