package server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import server.entities.dto.group.interview.Interview;
import server.entities.dto.group.organization.OrganizationApplication;
import server.entities.dto.group.project.ProjectApplication;
import server.repositories.group.organization.OrganizationApplicantRepository;
import server.repositories.group.project.ProjectApplicantRepository;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class ApplicantUpdateService {

  @Autowired
  private ProjectApplicantRepository projectApplicantRepository;

  @Autowired
  private OrganizationApplicantRepository organizationApplicantRepository;

  @Scheduled(fixedDelay = 5L * 60L * 1000L) // runs once every 5 minutes; in milliseconds
  public void interviewScheduleUpdater() throws InterruptedException {
    List<ProjectApplication> projectApplicants = projectApplicantRepository.getApplicantsByStatus("interview_scheduled");
    for (ProjectApplication projectApplicant : projectApplicants) {
      Interview interview = projectApplicant.getInterview();
      if (interview == null || interview.getEndDateTime() == null || interview.getStartDateTime() == null) {
        continue;
      }
      ZonedDateTime now = ZonedDateTime.now();
      if (now.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime().isAfter(interview.getEndDateTime())) {
        projectApplicant.setStatus("interviewed");
        projectApplicantRepository.save(projectApplicant);
      }
    }
    List<OrganizationApplication> organizationApplicants = organizationApplicantRepository.getApplicantsByStatus("interview_scheduled");
    for (OrganizationApplication organizationApplicant : organizationApplicants) {
      Interview interview = organizationApplicant.getInterview();
      if (interview == null || interview.getEndDateTime() == null || interview.getStartDateTime() == null) {
        continue;
      }
      ZonedDateTime now = ZonedDateTime.now();
      if (now.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime().isAfter(interview.getEndDateTime())) {
        organizationApplicant.setStatus("interviewed");
        organizationApplicantRepository.save(organizationApplicant);
      }
    }
  }
}
