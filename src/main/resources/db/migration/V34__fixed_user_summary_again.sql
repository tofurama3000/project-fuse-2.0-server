CREATE
OR REPLACE ALGORITHM = UNDEFINED
SELECT
  organization.id as id,
  SUM(user_profile.has_thumbnail) as num_members_no_thumbnail,
  SUM(user_profile.has_headline) as num_members_no_headline,
  SUM(user_profile.has_summary) as num_members_no_summary,
  COUNT(member.id) as num_members
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
     case when user_profile.thumbnail_id = '' OR user_profile.thumbnail_id IS NULL OR user_profile.thumbnail_id = 0 then 1 else 0 end has_thumbnail,
     case when user_profile.headline = '' OR user_profile.headline IS NULL then 1 else 0 end has_headline,
     case when user_profile.summary = '' OR user_profile.summary IS NULL then 1 else 0 end has_summary
   FROM
     user_profile
  ) as user_profile ON
                      user_profile.id = member.user_profile_id
GROUP BY
  organization.id;