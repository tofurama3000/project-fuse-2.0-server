ALTER TABLE interview
  ADD COLUMN code VARCHAR(36) NOT NULL;

CREATE INDEX code_index ON interview (code(36));