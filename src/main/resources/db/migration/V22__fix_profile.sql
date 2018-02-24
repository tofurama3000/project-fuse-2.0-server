ALTER table project_profile
  CHANGE COLUMN `thumbnail_id` `thumbnail_id` INT(11) NULL DEFAULT NULL ,
  CHANGE COLUMN `background_Id` `background_id` INT(11) NULL DEFAULT NULL ;

ALTER table team_profile
  CHANGE COLUMN `thumbnail_id` `thumbnail_id` INT(11) NULL DEFAULT NULL ,
  CHANGE COLUMN `background_Id` `background_id` INT(11) NULL DEFAULT NULL ;

ALTER table organization_profile
  CHANGE COLUMN `thumbnail_id` `thumbnail_id` INT(11) NULL DEFAULT NULL ,
  CHANGE COLUMN `background_Id` `background_id` INT(11) NULL DEFAULT NULL ;

ALTER table user_profile
  CHANGE COLUMN `thumbnail_id` `thumbnail_id` INT(11) NULL DEFAULT NULL ,
  CHANGE COLUMN `background_Id` `background_id` INT(11) NULL DEFAULT NULL ;
