CREATE TABLE IF NOT EXISTS interview_template (
    id INT(11) PRIMARY KEY AUTO_INCREMENT,
    organization_id INT(11),
    start_time DATETIME,
    end_time DATETIME
);

ALTER TABLE interview_template ADD COLUMN project_id INT(11);