CREATE TABLE IF NOT EXISTS files (
  id INT PRIMARY KEY AUTO_INCREMENT,
  file_name VARCHAR(255),
  hash VARCHAR(300),
  mime_type VARCHAR(255),
  file_size INT,
  upload_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  user_id INT,
  FOREIGN KEY (user_id) REFERENCES user(id)
);