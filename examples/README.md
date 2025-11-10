# examples/ — quick demo for dataset-prefixed env vars

What these examples show
- How RunJob provisions dataset-prefixed environment variables (e.g., bobin_hostname, bobin_username, bobin_password, bobout_*)
- How a simple ETL script can read those variables and connect to the dataset using the decrypted password supplied at runtime.
- Sample data in dataflow.datastatus that demonstrates IN vs OUT locktypes and how partitions may be READY or RUNNING.

Files
- myETL.sh — demo ETL script that reads dataset-prefixed variables (examples assume prefixes "bobin" and "bobout")
- seed_datastatus.sql — seeds example rows you provided into dataflow.datastatus
- seed_dataset_and_job.sql — seeds dataset and job tables so the example variables can be resolved

How to run the demo locally
1. Start Postgres and create the dataflow schema / tables:
   - See docs/create_tables.sql and docs/datamodel.txt; apply those before running these seeds.

2. Seed dataset and job tables:
   psql -U etl -d dataflow -f examples/seed_dataset_and_job.sql

3. Seed datastatus:
   psql -U etl -d dataflow -f examples/seed_datastatus.sql

4. Make demo script executable:
   chmod +x examples/myETL.sh

5. Run a job through RunJob:
   RunJob examples/myETL.sh
   (RunJob will provision the dataset-prefixed environment variables and the partition id, then invoke the script for the next READY partition.)

Notes
- The examples are intentionally minimal and safe; they demonstrate the variable naming convention and usage pattern. In production you should not print or persist decrypted passwords; treat the values as sensitive.
- If your installation uses slightly different variable names (e.g., uses *_encryptedpass instead of *_password), tell me the exact names and I'll update the example to match them.
