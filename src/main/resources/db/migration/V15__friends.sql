ALTER table notification
  ADD COLUMN object_Type VARCHAR(60),
    ADD COLUMN object_Id INT(11);


CREATE TABLE IF NOT EXISTS friend (
  id INT(11) PRIMARY KEY AUTO_INCREMENT,
  `sender_id` INT NOT NULL,
  `receiver_id` INT NOT NULL,
  status varchar(10),
  FOREIGN KEY (`receiver_id`)
  REFERENCES `user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  FOREIGN KEY (`sender_id`)
  REFERENCES `user` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION
);