ALTER TABLE `notification`
  CHANGE COLUMN `time` `time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
   DROP  COLUMN sender_id,
   DROP  COLUMN hasRead,
   ADD COLUMN deleted BIT,
   DROP  FOREIGN KEY notification_ibfk_1;