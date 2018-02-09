CREATE TABLE IF not EXISTS link (
    id INT(11) PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50),
    link VARCHAR(100),
    referenced_id INT(11),
    referenced_type VARCHAR(60)
);
