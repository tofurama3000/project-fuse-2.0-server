ALTER TABLE message ADD time DATETIME;
ALTER TABLE user ADD user_profile_id INT(11);
ALTER TABLE team ADD team_profile_id INT(11);
ALTER TABLE project ADD project_profile_id INT(11);
ALTER TABLE organization ADD organization_profile_id INT(11);

CREATE TABLE IF NOT EXISTS notification (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  sender_id INT(11),
  receiver_id INT(11),
  message TEXT,
  time DATETIME,
  hasRead    tinyint
);

CREATE TABLE IF NOT EXISTS project_profile (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  headline varchar(100),
  summary varchar(300),
  project_id INT(11)
);

CREATE TABLE IF NOT EXISTS team_profile (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  headline varchar(100),
  summary varchar(300),
  team_id INT(11)
);

CREATE TABLE IF NOT EXISTS organization_profile (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  headline varchar(100),
  summary varchar(300),
  organization_id INT(11)
);

CREATE TABLE IF NOT EXISTS user_profile (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  headline varchar(100),
  summary varchar(300),
  skills TEXT,
  user_id INT(11)
);


