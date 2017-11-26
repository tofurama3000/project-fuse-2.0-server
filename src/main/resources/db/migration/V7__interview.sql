CREATE TABLE IF NOT EXISTS interview (
    id INT(11) PRIMARY KEY AUTO_INCREMENT,
    start_time TIMESTAMP,
    end_time TIMESTAMP
);

ALTER table project_invitation
    ADD(interview_id INT(11),
    type varchar(20));

ALTER table project_invitation
    ADD CONSTRAINT FOREIGN KEY (interview_id) REFERENCES interview(id);

UPDATE project_invitation
set type = 'Join';

ALTER table team_invitation
    ADD(interview_id INT(11),
    type varchar(20));

ALTER table team_invitation
    ADD CONSTRAINT FOREIGN KEY (interview_id) REFERENCES interview(id);

UPDATE team_invitation
set type = 'Join';

ALTER table organization_invitation
    ADD(interview_id INT(11),
    type varchar(20));

ALTER table organization_invitation
    ADD CONSTRAINT FOREIGN KEY (interview_id) REFERENCES interview(id);

UPDATE organization_invitation
set type = 'Join';

