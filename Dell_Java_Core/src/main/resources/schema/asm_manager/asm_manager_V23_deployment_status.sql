ALTER TABLE deployment ADD COLUMN status VARCHAR(255);
UPDATE deployment SET status = 'COMPLETE';
ALTER TABLE deployment ALTER COLUMN status SET NOT NULL;
