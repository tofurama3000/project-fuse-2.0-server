CREATE TABLE IF NOT EXISTS team_applicant (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  sender_id INT(11),
  team_id INT(11),
  status varchar(10),
  time DATETIME,
  FOREIGN KEY (sender_id) REFERENCES user(id),
  FOREIGN KEY (team_id) REFERENCES team(id)
);

CREATE TABLE IF NOT EXISTS project_applicant(
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  sender_id INT(11),
  project_id INT(11),
  status varchar(10),
  time DATETIME,
  FOREIGN KEY (sender_id) REFERENCES user(id),
  FOREIGN KEY (project_id) REFERENCES project(id)
);

CREATE TABLE IF NOT EXISTS organization_applicant (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  sender_id INT(11),
  organization_id INT(11),
  status varchar(10),
  time DATETIME,
  FOREIGN KEY (sender_id) REFERENCES user(id),
  FOREIGN KEY (organization_id) REFERENCES organization(id)
);