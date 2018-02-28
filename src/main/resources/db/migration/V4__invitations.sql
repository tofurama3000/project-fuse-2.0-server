CREATE TABLE IF NOT EXISTS team_invitation (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  sender_id INT(11),
  receiver_id INT(11),
  team_id INT(11),
  status varchar(10)
);

CREATE TABLE IF NOT EXISTS project_invitation (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  sender_id INT(11),
  receiver_id INT(11),
  project_id INT(11),
  status varchar(10)
);

CREATE TABLE IF NOT EXISTS organization_invitation (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  sender_id INT(11),
  receiver_id INT(11),
  organization_id INT(11),
  status varchar(10)
);