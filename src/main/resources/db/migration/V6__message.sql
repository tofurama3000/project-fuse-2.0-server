CREATE TABLE IF NOT EXISTS message (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  sender_id INT(11),
  receiver_id INT(11),
  message TEXT
);