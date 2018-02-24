ALTER table project_applicant
  ADD COLUMN `interview_id` INT(11) NOT NULL DEFAULT 0,
  ADD FOREIGN KEY interview_key (`interview_id`) REFERENCES `interview`(`id`);

ALTER table organization_applicant
  ADD COLUMN `interview_id` INT(11) NOT NULL DEFAULT 0,
  ADD FOREIGN KEY interview_key (`interview_id`) REFERENCES `interview`(`id`);

ALTER table team_applicant
  ADD COLUMN `interview_id` INT(11) NOT NULL DEFAULT 0,
  ADD FOREIGN KEY interview_key (`interview_id`) REFERENCES `interview`(`id`);

ALTER table interview
  ADD COLUMN `deleted` TINYINT(1) NOT NULL DEFAULT 0;