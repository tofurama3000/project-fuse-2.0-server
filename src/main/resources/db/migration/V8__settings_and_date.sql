ALTER table interview
    MODIFY COLUMN start_time DATETIME;

ALTER table interview
    MODIFY COLUMN end_time DATETIME;

ALTER table interview
    ADD group_type VARCHAR(20);

CREATE table if not EXISTS team_settings(
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  group_type varchar(10),
  group_id INT(11),
  FOREIGN KEY (group_id) REFERENCES team(id)
);

CREATE table if not EXISTS project_settings(
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  group_type varchar(10),
  group_id INT(11),
  FOREIGN KEY (group_id) REFERENCES team(id)
);

CREATE table if not EXISTS organization_settings(
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  group_type varchar(10),
  group_id INT(11),
  FOREIGN KEY (group_id) REFERENCES organization(id)
);

--CREATE table if not EXISTS team_interview_slots(
--  id INT(11) PRIMARY KEY AUTO_INCREMENT,
--  team_id INT(11),
--  interview_id INT(11),
--  FOREIGN KEY (team_id) REFERENCES team(id),
--  FOREIGN KEY (interview_id) REFERENCES interview(id)
--);
--
--CREATE table if not EXISTS organization_interview_slots(
--  id INT(11) PRIMARY KEY AUTO_INCREMENT,
--  organization_id INT(11),
--  interview_id INT(11),
--  FOREIGN KEY (organization_id) REFERENCES organization(id),
--  FOREIGN KEY (interview_id) REFERENCES interview(id)
--);
--
--CREATE table if not EXISTS project_interview_slots(
--  id INT(11) PRIMARY KEY AUTO_INCREMENT,
--  project_id INT(11),
--  interview_id INT(11),
--  FOREIGN KEY (project_id) REFERENCES project(id),
--  FOREIGN KEY (interview_id) REFERENCES interview(id)
--);

