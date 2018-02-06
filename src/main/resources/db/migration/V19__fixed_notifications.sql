ALTER TABLE `project_fuse`.`notification`
  DROP COLUMN `hasRead`;
ALTER TABLE `project_fuse`.`notification`
  CHANGE COLUMN `object_Type` `object_type` VARCHAR(60) NULL DEFAULT NULL ,
  CHANGE COLUMN `object_Id` `object_id` INT(11) NULL DEFAULT NULL ,
  ADD COLUMN `has_read` TINYINT(1) NULL DEFAULT 0;
