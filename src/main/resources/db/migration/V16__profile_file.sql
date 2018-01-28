ALTER table project_profile
  ADD COLUMN thumbnail_id INT(11) NOT NULL DEFAULT 0,
    ADD COLUMN background_Id INT(11) NOT NULL DEFAULT 0;

ALTER table team_profile
  ADD COLUMN thumbnail_id INT(11) NOT NULL DEFAULT 0,
  ADD COLUMN background_Id INT(11) NOT NULL DEFAULT 0;


ALTER table organization_profile
  ADD COLUMN thumbnail_id INT(11) NOT NULL DEFAULT 0,
  ADD COLUMN background_Id INT(11) NOT NULL DEFAULT 0;


ALTER table user_profile
  ADD COLUMN thumbnail_id INT(11) NOT NULL DEFAULT 0,
  ADD COLUMN background_Id INT(11) NOT NULL DEFAULT 0;

