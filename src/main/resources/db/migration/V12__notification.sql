CREATE TABLE IF NOT EXISTS notification (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  receiver_id INT(11),
  message TEXT,
  time DATETIME,
  FOREIGN KEY (receiver_id ) REFERENCES user(id)
);