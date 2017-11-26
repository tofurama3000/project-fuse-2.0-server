CREATE TABLE IF NOT EXISTS Notification (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  sender_id INT(11),
  receiver_id INT(11),
  message TEXT,
  time TIMESTAMP,
  hasRead    tinyint,
  FOREIGN KEY (sender_id ) REFERENCES user(id),
  FOREIGN KEY (receiver_id ) REFERENCES user(id)
);

ALTER TABLE message ADD time timestamp;