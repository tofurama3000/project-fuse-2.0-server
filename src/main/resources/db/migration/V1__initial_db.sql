CREATE DATABASE IF NOT EXISTS project_fuse;

CREATE TABLE IF NOT EXISTS user (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  name TEXT,
  encoded_password TEXT,
  email VARCHAR(500)
);


CREATE TABLE IF NOT EXISTS session (
  session_id varchar(32) PRIMARY KEY,
  created TIMESTAMP,
  email VARCHAR(500)
);