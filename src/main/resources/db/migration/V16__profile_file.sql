ALTER table project_profile
  ADD COLUMN thumbnail_id INT(11),
    ADD COLUMN backgroud_Id INT(11);

ALTER table team_profile
  ADD COLUMN thumbnail_id INT(11),
  ADD COLUMN backgroud_Id INT(11);


ALTER table organization_profile
  ADD COLUMN thumbnail_id INT(11),
  ADD COLUMN backgroud_Id INT(11);


ALTER table user_profile
  ADD COLUMN thumbnail_id INT(11),
  ADD COLUMN backgroud_Id INT(11);

