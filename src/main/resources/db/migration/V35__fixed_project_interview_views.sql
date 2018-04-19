
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
    user AS member ON member.id = interview.user_id
  GROUP BY
    project.name, interview.start_time ASC;


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
                                          used_project_interview.user_id IS NOT NULL AND
                                          used_project_interview.start_time > NOW()
  WHERE
    organization.deleted = 0 AND project.deleted = 0 AND project.organization_id IS NOT NULL
  GROUP BY
    project.id, organization.id;