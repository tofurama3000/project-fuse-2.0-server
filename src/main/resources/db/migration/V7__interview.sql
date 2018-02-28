CREATE TABLE IF NOT EXISTS interview (
    id INT(11) PRIMARY KEY AUTO_INCREMENT,
    start_time DATETIME,
    end_time DATETIME
);

ALTER table project_invitation
    ADD(interview_id INT(11),
    type varchar(20));


UPDATE project_invitation
set type = 'Join';

ALTER table team_invitation
    ADD(interview_id INT(11),
    type varchar(20));

UPDATE team_invitation
set type = 'Join';

ALTER table organization_invitation
    ADD(interview_id INT(11),
    type varchar(20));

UPDATE organization_invitation
set type = 'Join';

