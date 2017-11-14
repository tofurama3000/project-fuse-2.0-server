CREATE TABLE IF NOT EXISTS teamInvitation (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  sender_id INT(11),
  receiver_id INT(11),
  team_id INT(11)
  varchar(10) status,
  FOREIGN KEY (sender_id) REFERENCES user(id),
  FOREIGN KEY (receiver_id) REFERENCES user(id),
  FOREIGN KEY (team_id) REFERENCES team(id)
);

CREATE TABLE IF NOT EXISTS projectInvitation (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  sender_id INT(11),
  receiver_id INT(11),
  project_id INT(11)
  varchar(10) status,
  FOREIGN KEY (sender_id) REFERENCES user(id),
  FOREIGN KEY (receiver_id) REFERENCES user(id),
  FOREIGN KEY (project_id) REFERENCES project(id)
);

CREATE TABLE IF NOT EXISTS organizationInvitation (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  sender_id INT(11),
  receiver_id INT(11),
  organization_id INT(11)
  varchar(10) status,
  FOREIGN KEY (sender_id) REFERENCES user(id),
  FOREIGN KEY (receiver_id) REFERENCES user(id),
  FOREIGN KEY (organization_id) REFERENCES organization(id)
);