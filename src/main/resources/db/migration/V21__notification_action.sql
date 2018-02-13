ALTER table notification
  ADD COLUMN `action_done` TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN `notification_type` VARCHAR(60) NOT NULL DEFAULT 0,
  CHANGE COLUMN `object_Type` `data_type` VARCHAR(60);

ALTER table project_invitation
  ADD COLUMN `applicant_id` INT(11) NOT NULL DEFAULT 0,
  ADD FOREIGN KEY project_applicant_key (`applicant_id`) REFERENCES `project_applicant`(`id`);

ALTER table organization_invitation
  ADD COLUMN `applicant_id` INT(11) NOT NULL DEFAULT 0,
  ADD FOREIGN KEY organization_applicant_key (`applicant_id`) REFERENCES `organization_applicant`(`id`);

ALTER table team_invitation
  ADD COLUMN `applicant_id` INT(11) NOT NULL DEFAULT 0,
  ADD FOREIGN KEY team_applicant_key (`applicant_id`) REFERENCES `team_applicant`(`id`);