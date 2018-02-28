CREATE TABLE IF NOT EXISTS team_applicant (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  sender_id INT(11),
  team_id INT(11),
  status varchar(10),
  time DATETIME
);

CREATE TABLE IF NOT EXISTS project_applicant(
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  sender_id INT(11),
  project_id INT(11),
  status varchar(10),
  time DATETIME
);

CREATE TABLE IF NOT EXISTS organization_applicant (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  sender_id INT(11),
  organization_id INT(11),
  status varchar(10),
  time DATETIME
);