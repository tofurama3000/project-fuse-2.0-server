ALTER table interview
    ADD COLUMN user_id INT(11);

ALTER table interview
    ADD CONSTRAINT FOREIGN KEY (user_id) REFERENCES user(id);
