ALTER table project_applicant
  ADD COLUMN `interview_id` INT(11) NOT NULL DEFAULT 0;

ALTER table organization_applicant
  ADD COLUMN `interview_id` INT(11) NOT NULL DEFAULT 0;

ALTER table team_applicant
  ADD COLUMN `interview_id` INT(11) NOT NULL DEFAULT 0;

ALTER table interview
  ADD COLUMN `deleted` TINYINT(1) NOT NULL DEFAULT 0;