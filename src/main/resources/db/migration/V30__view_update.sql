# Ensure views are in place before drop
CREATE OR REPLACE VIEW member_organization_interview_project_breakdown AS SELECT * from project;
CREATE OR REPLACE VIEW member_organization_interview_project_summary AS SELECT * from project;
CREATE OR REPLACE VIEW organization_interview_project_breakdown AS SELECT * from project;
CREATE OR REPLACE VIEW organization_interview_project_summary AS SELECT * from project;
CREATE OR REPLACE VIEW users_without_headline_breakdown AS SELECT * from project;
CREATE OR REPLACE VIEW users_without_summary_breakdown AS SELECT * from project;
CREATE OR REPLACE VIEW users_without_thumbnail_breakdown AS SELECT * from project;

# Drop old views
DROP VIEW member_organization_interview_project_breakdown;
DROP VIEW member_organization_interview_project_summary;
DROP VIEW organization_interview_project_breakdown;
DROP VIEW organization_interview_project_summary;
DROP VIEW users_without_headline_breakdown;
DROP VIEW users_without_summary_breakdown;
DROP VIEW users_without_thumbnail_breakdown;

# Create new views
CREATE
OR REPLACE ALGORITHM = UNDEFINED
VIEW `project_organization_interview_summary` AS
  SELECT
    CONCAT(project.id, '-', organization.id) AS id,
    project.id                               AS proj_id,
    project.name                             AS proj_name,
    organization.id                          AS org_id,
    COUNT(all_project_interview.id)          AS total_interviews,
    COUNT(used_project_interview.id)         AS used_interviews
  FROM
    project
    INNER JOIN
    organization ON organization.id = project.organization_id
    LEFT JOIN
    interview AS all_project_interview ON
                                         all_project_interview.group_type = 'Project' AND
                                         all_project_interview.group_id = project.id
    LEFT JOIN
    interview AS used_project_interview ON
                                          used_project_interview.group_type = 'Project' AND
                                          used_project_interview.group_id = project.id AND
                                          used_project_interview.user_id IS NULL AND
                                          used_project_interview.start_time > NOW()
  WHERE
    organization.deleted = 0 AND project.deleted = 0 AND project.organization_id IS NOT NULL
  GROUP BY
    project.id, organization.id;

CREATE
OR REPLACE ALGORITHM = UNDEFINED
VIEW member_project_organization_interview_breakdown AS
  SELECT
    CONCAT(member.id, '-', project.id, '-', organization.id) AS id,
    member.id           AS member_id,
    member.name         AS member_name,
    project.id          AS proj_id,
    project.name        AS proj_name,
    organization.id     AS org_id,
    COUNT(interview.id) AS num_interviews
  FROM
    organization
    INNER JOIN
    project ON project.organization_id = organization.id
    INNER JOIN
    (SELECT
       project_id,
       user_id,
       id
     FROM
       project_member
     GROUP BY
       user_id, project_id
    ) AS relationship ON
                        relationship.project_id = project.id
    INNER JOIN
    user AS member ON member.id = relationship.user_id
    LEFT JOIN
    interview ON
                interview.user_id = member.id AND
                interview.group_type = 'Project' AND
                interview.group_id = project.id AND
                interview.start_time > NOW()
  GROUP BY
    organization.id, member.id, project.id
  ORDER BY
    member.name, interview.start_time ASC;

CREATE
OR REPLACE ALGORITHM = UNDEFINED
VIEW project_organization_interview_breakdown AS
  SELECT
    CONCAT(member.id, '-', project.id, '-', organization.id) AS id,
    member.id       AS member_id,
    member.name     AS member_name,
    project.id      AS proj_id,
    project.name    AS proj_name,
    organization.id AS org_id,
    interview.start_time,
    interview.end_time,
    interview.available
  FROM
    organization
    INNER JOIN
    project ON project.organization_id = organization.id
    INNER JOIN
    (SELECT
       project_id,
       user_id,
       id
     FROM
       project_member
     GROUP BY
       user_id, project_id
    ) AS relationship ON
                        relationship.project_id = project.id
    INNER JOIN
    interview ON
                interview.group_type = 'Project' AND
                interview.group_id = project.id
    LEFT JOIN
    user AS member ON member.id = relationship.user_id AND member.id = interview.user_id
  GROUP BY
    project.name, interview.start_time ASC;

CREATE
OR REPLACE ALGORITHM = UNDEFINED
VIEW users_with_invalid_profiles_breakdown AS
  SELECT
    CONCAT(member.id, '-', organization.id) AS id,
    organization.id as org_id,
    member.id as member_id,
    member.name as member_name,
    user_profile.has_thumbnail as has_thumbnail,
    user_profile.has_headline as has_headline,
    user_profile.has_summary as has_summary
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
    (SELECT
       user_profile.id,
       case when user_profile.thumbnail_id = '' OR user_profile.thumbnail_id IS NULL OR user_profile.thumbnail_id = 0 then 0 else 1 end has_thumbnail,
       case when user_profile.headline = '' OR user_profile.headline IS NULL then 0 else 1 end has_headline,
       case when user_profile.summary = '' OR user_profile.summary IS NULL then 0 else 1 end has_summary
     FROM
       user_profile
    ) as user_profile ON
                        user_profile.id = member.user_profile_id
  GROUP BY
    organization.id, member.id;

CREATE
OR REPLACE ALGORITHM = UNDEFINED
VIEW users_with_invalid_profiles_summary AS
  SELECT
    organization.id AS id,
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



CREATE
OR REPLACE ALGORITHM = UNDEFINED
VIEW member_project_organization_interview_summary AS
  SELECT
    CONCAT(member.id, '-', organization.id) AS id,
    member.id as member_id,
    member.name as member_name,
    organization.id as org_id,
    COUNT(interview.id) as num_interviews,
    COUNT(interview.group_id) as num_projects_with_interviews
  FROM
    organization
    INNER JOIN
    organization_member ON organization_member.organization_id = organization.id
    INNER JOIN
    user as member ON member.id = organization_member.user_id
    LEFT JOIN
    project ON project.organization_id = organization.id
    LEFT JOIN
    (SELECT
       project_id, user_id, id
     FROM
       project_member
     GROUP BY
       user_id, project_id
    ) as relationship ON
                        relationship.project_id = project.id and relationship.id = member.id
    LEFT JOIN
    interview ON
                interview.group_type = 'Project' AND
                interview.group_id = project.id AND
                interview.user_id = member.id
  GROUP BY
    organization.id, member.id;
