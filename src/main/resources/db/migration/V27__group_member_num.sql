ALTER TABLE project
  ADD COLUMN num_members INT(11) DEFAULT 0;

ALTER TABLE organization
  ADD COLUMN num_members INT(11) DEFAULT 0;