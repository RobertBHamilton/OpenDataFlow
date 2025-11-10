-- examples/seed_dataset_and_job.sql
-- Seed dataset descriptors and job descriptors used by the demo.
-- Columns match the output of: ./utility.sh RunSql "select * from dataset" / "select * from job"

INSERT INTO dataflow.dataset (database, datasetid, encryptedpass, hostname, schemaname, tablename, username) VALUES
  ('dataflow','bobin','ZpLfE+uTYE2mdmjOPrukol3yu+cpAHBnmL6trHa9PHGj','localhost','testdata','bobdata','etl'),
  ('dataflow','bobout','ZpLfE+uTYE2mdmjOPrukol3yu+cpAHBnmL6trHa9PHGj','localhost','testdata','bobdata','etl')
ON CONFLICT (datasetid) DO NOTHING;

INSERT INTO dataflow.job (datasetid, itemtype, jobid, modified) VALUES
  ('bobout','OUT','loadbob','2025-11-05 18:20:14.14095'),
  ('bobin','IN','loadbob','2025-11-05 18:40:15.572576')
ON CONFLICT (datasetid, jobid) DO NOTHING;
