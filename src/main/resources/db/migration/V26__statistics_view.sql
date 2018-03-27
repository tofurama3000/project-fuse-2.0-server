CREATE TABLE IF NOT EXISTS interview_template(
  id INT NOT NULL auto_increment,
  start_time DATETIME,
  end_time DATETIME,
  organization_id INT NOT NULL,
  PRIMARY KEY (id)
);

ALTER TABLE friend
  ADD COLUMN deleted TINYINT(1) DEFAULT 0;

ALTER TABLE organization
  ADD COLUMN deleted TINYINT(1) DEFAULT 0;

ALTER TABLE project
  ADD COLUMN deleted TINYINT(1) DEFAULT 0;

ALTER TABLE files
  ADD COLUMN deleted TINYINT(1) DEFAULT 0;

ALTER TABLE link
  ADD COLUMN deleted TINYINT(1) DEFAULT 0;

CREATE VIEW organization_interview_project_summary AS
  SELECT
    project.id as proj_id,
    project.name as proj_name,
    organization.id as org_id,
    COUNT(all_project_interview.id) as total_interviews,
    COUNT(used_project_interview.id) as used_interviews
  FROM
    project
    INNER JOIN
    organization ON organization.id = project.organization_id
    LEFT JOIN
    interview as all_project_interview ON
                                         all_project_interview.group_type = 'Project' AND
                                         all_project_interview.group_id = project.id
    LEFT JOIN
    interview as used_project_interview ON
                                          used_project_interview.group_type = 'Project' AND
                                          used_project_interview.group_id = project.id AND
                                          used_project_interview.user_id = NULL
  WHERE
    organization.deleted = 0 AND project.deleted = 0 AND project.organization_id != NULL
  GROUP BY
    project.id;

CREATE VIEW member_organization_interview_project_summary AS
  SELECT
    member.id as member_id,
    member.name as member_name,
    project.id as proj_id,
    organization.id as org_id,
    COUNT(interview.id) as num_interviews
  FROM
    organization
    INNER JOIN
    project ON project.organization_id = organization.id
    INNER JOIN
    (SELECT
       project_id, user_id, id
     FROM
       project_member
     GROUP BY
       user_id, project_id
    ) as relationship ON
                        relationship.project_id = project.id
    INNER JOIN
    user as member ON member.id = relationship.user_id
    LEFT JOIN
    interview ON
                interview.user_id = member.id AND
                interview.group_type = 'Project' AND
                interview.group_id = project.id
  GROUP BY
    member.id;

CREATE VIEW organization_interview_project_breakdown AS
  SELECT
    member.id as member_id,
    member.name as member_name,
    project.id as proj_id,
    organization.id as org_id,
    interview.start_time,
    interview.end_time,
    interview.available
  FROM
    organization
    INNER JOIN
    project ON project.organization_id = organization.id
    INNER JOIN
    (SELECT
       project_id, user_id, id
     FROM
       project_member
     GROUP BY
       user_id, project_id
    ) as relationship ON
                        relationship.project_id = project.id
    INNER JOIN
    user as member ON member.id = relationship.user_id
    INNER JOIN
    interview ON
                interview.user_id = member.id AND
                interview.group_type = 'Project' AND
                interview.group_id = project.id
  GROUP BY
    interview.id;

CREATE VIEW member_organization_interview_project_breakdown AS
  SELECT
    member.id as member_id,
    member.name as member_name,
    project.id as proj_id,
    organization.id as org_id,
    interview.start_time,
    interview.end_time,
    interview.available
  FROM
    organization
    INNER JOIN
    project ON project.organization_id = organization.id
    INNER JOIN
    (SELECT
       project_id, user_id, id
     FROM
       project_member
     GROUP BY
       user_id, project_id
    ) as relationship ON
                        relationship.project_id = project.id
    INNER JOIN
    user as member ON member.id = relationship.user_id
    INNER JOIN
    interview ON
                interview.user_id = member.id AND
                interview.group_type = 'Project' AND
                interview.group_id = project.id
  GROUP BY
    interview.id;

CREATE VIEW users_without_summary_breakdown AS
  SELECT
    organization_id,
    member.id,
    member.name
  FROM
    organization
    INNER JOIN
    (SELECT
       organization_id, user_id, id
     FROM
       organization_member
     GROUP BY
       organization_id
    ) as relationship ON
                        relationship.organization_id = organization.id
    INNER JOIN
    user as member
    INNER JOIN
    (SELECT user_profile.id
     FROM
       user_profile
     WHERE
       user_profile.summary = '' OR user_profile.summary IS NULL
    ) as user_profile ON
                        user_profile.id = member.user_profile_id
  GROUP BY
    organization.id, member.id;

CREATE VIEW users_without_headline_breakdown AS
  SELECT
    organization_id,
    member.id,
    member.name
  FROM
    organization
    INNER JOIN
    (SELECT
       organization_id, user_id, id
     FROM
       organization_member
     GROUP BY
       organization_id
    ) as relationship ON
                        relationship.organization_id = organization.id
    INNER JOIN
    user as member
    INNER JOIN
    (SELECT user_profile.id
     FROM
       user_profile
     WHERE
       user_profile.headline = '' OR user_profile.headline IS NULL
    ) as user_profile ON
                        user_profile.id = member.user_profile_id
  GROUP BY
    organization.id, member.id;

CREATE VIEW users_without_thumbnail_breakdown AS
  SELECT
    organization_id,
    member.id,
    member.name
  FROM
    organization
    INNER JOIN
    (SELECT
       organization_id, user_id, id
     FROM
       organization_member
     GROUP BY
       organization_id
    ) as relationship ON
                        relationship.organization_id = organization.id
    INNER JOIN
    user as member
    INNER JOIN
    (SELECT user_profile.id
     FROM
       user_profile
     WHERE
       user_profile.thumbnail_id = '' OR user_profile.thumbnail_id IS NULL OR user_profile.thumbnail_id = 0
    ) as user_profile ON
                        user_profile.id = member.user_profile_id
  GROUP BY
    organization.id, member.id;

CREATE VIEW users_with_invalid_profiles_summary AS
  SELECT
    organization_id,
    count(user_profile_no_summary.id) as num_members_no_summary,
    count(user_profile_no_headline.id) as num_members_no_headline,
    count(user_profile_no_thumbnail.id) as num_members_no_thumbnail,
    count(member.id) as num_members
  FROM
    organization
    INNER JOIN
    (SELECT
       organization_id, user_id, id
     FROM
       organization_member
     GROUP BY
       organization_id
    ) as relationship ON
                        relationship.organization_id = organization.id
    INNER JOIN
    user as member
    LEFT JOIN
    (SELECT user_profile.id
     FROM
       user_profile
     WHERE
       user_profile.summary = '' OR user_profile.summary IS NULL
    ) as user_profile_no_summary ON
                                   user_profile_no_summary.id = member.user_profile_id
    LEFT JOIN
    (SELECT user_profile.id
     FROM
       user_profile
     WHERE
       user_profile.headline = '' OR user_profile.headline IS NULL
    ) as user_profile_no_headline ON
                                    user_profile_no_headline.id = member.user_profile_id
    LEFT JOIN
    (SELECT user_profile.id
     FROM
       user_profile
     WHERE
       user_profile.thumbnail_id = '' OR user_profile.thumbnail_id IS NULL OR user_profile.thumbnail_id = 0
    ) as user_profile_no_thumbnail ON
                                     user_profile_no_thumbnail.id = member.user_profile_id
  GROUP BY
    organization.id;
