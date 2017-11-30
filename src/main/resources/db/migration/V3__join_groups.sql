ALTER TABLE project
  ADD restriction varchar(10);

ALTER TABLE team
  ADD restriction varchar(10);

ALTER TABLE organization
  ADD restriction varchar(10);

ALTER TABLE project_member
  ADD role_id int;

ALTER TABLE team_member
  ADD role_id int;

ALTER TABLE organization_member
  ADD role_id int;




