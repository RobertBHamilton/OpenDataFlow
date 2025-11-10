-- examples/seed_datastatus.sql
-- Insert sample rows into dataflow.datastatus demonstrating IN vs OUT locktypes and statuses.
INSERT INTO dataflow.datastatus (dataid, datasetid, jobid, locktype, modified, status) VALUES
  ('1.1','bobin','loadbob','IN','2025-11-05 18:52:26.372454','READY'),
ON CONFLICT (dataid, datasetid, jobid) DO NOTHING;
