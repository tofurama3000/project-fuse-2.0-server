CREATE DATABASE IF NOT EXISTS project_fuse;

CREATE TABLE IF NOT EXISTS user (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  name TEXT,
  global_role TEXT,
  encoded_password TEXT,
  email VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS role (
  id INT(11) PRIMARY KEY,
  name TEXT
);

CREATE TABLE IF NOT EXISTS organization (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  name TEXT
);

CREATE TABLE IF NOT EXISTS project (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  name TEXT
);

CREATE TABLE IF NOT EXISTS team (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  team_owner_id INT(11),
  name TEXT,
  FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE IF NOT EXISTS team (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  name TEXT
);

CREATE TABLE IF NOT EXISTS session (
  session_id varchar(200) PRIMARY KEY,
  created TIMESTAMP,
  email VARCHAR(500)
);

CREATE TABLE team_member (
    id int PRIMARY KEY AUTO_INCREMENT,
    team_id int NOT NULL,
    user_id int NOT NULL,
    FOREIGN KEY (team_id) REFERENCES team(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE project_member (
    id int PRIMARY KEY AUTO_INCREMENT,
    project_id int NOT NULL,
    user_id int not NULL,
    FOREIGN KEY (project_id) REFERENCES project(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE organization_member (
    id int PRIMARY KEY AUTO_INCREMENT,
    organization_id int NOT NULL,
    user_id int not NULL,
    FOREIGN KEY (organization_id) REFERENCES organization(id),
    FOREIGN KEY (user_id) REFERENCES user(id)
);


