
CREATE
OR REPLACE ALGORITHM = UNDEFINED
VIEW `project_organization_interview_summary` AS
    SELECT
      CONCAT(project.id, '-', organization.id) AS id,
      project.id                               AS proj_id,
      project.name                             AS proj_name,
      organization.id                          AS org_id,
      case when interview.val IS NOT NULL then SUM(interview.val) else 0 end AS total_interviews,
      case when interview.user_val IS NOT NULL then SUM(interview.user_val) else 0 end AS used_interviews
    FROM
      project
      INNER JOIN
      organization ON organization.id = project.organization_id
      LEFT JOIN
      (SELECT group_id, 1 as val, case when user_id = 0 OR user_id IS NULL then 0 else 1 end as user_val
       FROM interview
       WHERE interview.group_type = 'Project'
      ) AS interview ON interview.group_id = project.id
    WHERE
      organization.deleted = 0 AND project.deleted = 0 AND project.organization_id IS NOT NULL
    GROUP BY
      project.id, organization.id;


CREATE
OR REPLACE ALGORITHM = UNDEFINED
VIEW project_organization_interview_breakdown as
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
    interview.available,
    interview.user_id
  FROM
    organization
  INNER JOIN
    project ON project.organization_id = organization.id
  INNER JOIN
    interview ON
    interview.group_type = 'Project' AND
    interview.group_id = project.id AND
    interview.start_time > NOW()
  LEFT JOIN
  user AS member ON member.id = interview.user_id
  group by
    interview.id
  ORDER BY
    project.name, interview.start_time ASC;