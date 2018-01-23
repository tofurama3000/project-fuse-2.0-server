DROP TABLE IF EXISTS `notification`;

CREATE TABLE `notification` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `receiver_id` INT NOT NULL,
  `message` TEXT,
  `time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `hasRead` TINYINT(1) NOT NULL DEFAULT 0,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `notification_receiver_idx` (`receiver_id` ASC),
  CONSTRAINT `notification_receiver`
  FOREIGN KEY (`receiver_id`)
  REFERENCES `user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
