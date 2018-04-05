CREATE TABLE IF NOT EXISTS interview_template (
    id INT(11) PRIMARY KEY AUTO_INCREMENT,
    project_id INT(11),
    organization_id INT(11),
    start_time DATETIME,
    end_time DATETIME
);

