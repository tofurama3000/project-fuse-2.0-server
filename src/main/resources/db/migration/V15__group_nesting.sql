ALTER TABLE project
  ADD organization_id INT(11);

ALTER table project
    ADD CONSTRAINT FOREIGN KEY (organization_id) REFERENCES organization(id);
