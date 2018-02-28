ALTER table project_applicant
  ADD COLUMN `interview_id` INT(11);

ALTER table organization_applicant
  ADD COLUMN `interview_id` INT(11);

ALTER table team_applicant
  ADD COLUMN `interview_id` INT(11);

ALTER table interview
  ADD COLUMN `deleted` TINYINT(1);