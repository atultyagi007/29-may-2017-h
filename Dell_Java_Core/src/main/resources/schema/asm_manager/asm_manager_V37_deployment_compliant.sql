ALTER TABLE deployment
ADD COLUMN compliant boolean NOT NULL DEFAULT true;

UPDATE deployment
SET compliant = false, status = 'COMPLETE'
WHERE status = 'NONCOMPLIANT';