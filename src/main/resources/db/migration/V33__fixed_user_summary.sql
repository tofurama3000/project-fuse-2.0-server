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
      user as member ON member.id = relationship.user_id
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