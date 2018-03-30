ALTER TABLE project
  ADD COLUMN number_of_members INT(11) DEFAULT 0;

ALTER TABLE organization
  ADD COLUMN number_of_members INT(11) DEFAULT 0;