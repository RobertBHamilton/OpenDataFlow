-- examples/seed_datastatus.sql
-- Insert sample rows into dataflow.datastatus demonstrating IN vs OUT locktypes and statuses.
INSERT INTO dataflow.datastatus (dataid, datasetid, jobid, locktype, modified, status) VALUES
  ('1.0','bobout','loadbob','OUT','2025-11-05 17:44:28.830406','READY'),
  ('1.1','bobin','otherjob','OUT','2025-11-05 18:51:25.847234','READY'),
  ('1.1','bobin','loadbob','IN','2025-11-05 18:52:26.372454','RUNNING'),
  ('1.2','bobin','otherjob','OUT','2025-11-05 18:51:32.666023','READY'),
  ('1.2','bobin','loadbob','IN','2025-11-10 13:58:14.460626','RUNNING'),
  ('1.2','bobout','loadbob','OUT','2025-11-10 13:58:14.473988','RUNNING')
ON CONFLICT (dataid, datasetid, jobid) DO NOTHING;
