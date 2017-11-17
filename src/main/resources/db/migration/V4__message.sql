CREATE TABLE IF NOT EXISTS Message (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  sender_id INT(11),
  receiver_id INT(11),
  message TEXT,
  FOREIGN KEY (sender_id ) REFERENCES user(id),
  FOREIGN KEY (receiver_id ) REFERENCES user(id)
);