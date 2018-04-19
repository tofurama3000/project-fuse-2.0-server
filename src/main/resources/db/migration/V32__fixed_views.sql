
CREATE
OR REPLACE ALGORITHM = UNDEFINED
VIEW project_organization_interview_breakdown AS
  SELECT
    CONCAT(
        interview.id, '-', project.id, '-', organization.id) AS id,
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
VIEW member_project_organization_interview_breakdown AS
  SELECT
    CONCAT(member.id, '-', '-', organization.id) AS id,
    member.id           AS member_id,
    member.name         AS member_name,
    organization.id     AS org_id,
    COUNT(interview.id) AS num_interviews
  FROM
    organization
    INNER JOIN
    (SELECT
       organization_id,
       user_id
     FROM
       organization_member
     GROUP BY
       user_id, organization_id
    ) as orgRelationship ON orgRelationship.organization_id = organization.id
    INNER JOIN
    user AS member ON member.id = orgRelationship.user_id
    LEFT JOIN
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
    LEFT JOIN
    interview ON
                interview.user_id = member.id AND
                interview.group_type = 'Project' AND
                interview.group_id = project.id AND
                interview.start_time > NOW()
  GROUP BY
    organization.id, member.id
  ORDER BY
    member.name, interview.start_time ASC;


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
       user_id, organization_id
    ) as relationship ON
                        relationship.organization_id = organization.id
    INNER JOIN
    user as member ON member.id = relationship.user_id
    LEFT JOIN
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