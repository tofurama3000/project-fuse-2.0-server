ALTER table interview
    MODIFY COLUMN start_time DATETIME;

ALTER table interview
    MODIFY COLUMN end_time DATETIME;

alter table interview
    ADD available char(1);

ALTER table interview
    ADD group_type VARCHAR(20);

CREATE table if not EXISTS team_settings(
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  group_type varchar(10),
  group_id INT(11)
);

CREATE table if not EXISTS project_settings(
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  group_type varchar(10),
  group_id INT(11)
);

CREATE table if not EXISTS organization_settings(
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  group_type varchar(10),
  group_id INT(11)
);

CREATE table if not EXISTS taken_interview(
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  interview_id INT(11),
  user_id INT(11)
);

