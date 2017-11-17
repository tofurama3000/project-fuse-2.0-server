ALTER TABLE user
  ADD registration_status char(1);

CREATE TABLE IF NOT EXISTS unregistered_user (
  user_id INT(11) PRIMARY KEY AUTO_INCREMENT,
  registration_key varchar(50),
  FOREIGN KEY (user_id) REFERENCES user(id)
)