ALTER TABLE deployment ADD COLUMN brownfield boolean NOT NULL DEFAULT false;

UPDATE deployment SET brownfield = false;
