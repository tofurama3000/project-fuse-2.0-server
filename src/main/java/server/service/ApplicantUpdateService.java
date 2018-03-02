package server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import server.entities.dto.group.interview.Interview;
import server.entities.dto.group.organization.OrganizationApplicant;
import server.entities.dto.group.project.ProjectApplicant;
import server.repositories.group.organization.OrganizationApplicantRepository;
import server.repositories.group.project.ProjectApplicantRepository;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class ApplicantUpdateService {

  @Autowired
  ProjectApplicantRepository projectApplicantRepository;

  @Autowired
  OrganizationApplicantRepository organizationApplicantRepository;

  @Scheduled(fixedDelay = 5L * 60L * 1000L) // runs once every 5 minutes; in milliseconds
  public void StatusChanger() throws InterruptedException {
    List<ProjectApplicant> list = projectApplicantRepository.getApplicantsByStatus("interview_scheduled");
    for (ProjectApplicant a : list) {
      Interview interview = a.getInterview();
      ZonedDateTime now = ZonedDateTime.now();
      if (now.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime().isAfter(interview.getEndDateTime())) {
        a.setStatus("interviewed");
        projectApplicantRepository.save(a);
      }
    }
    List<OrganizationApplicant> oList = organizationApplicantRepository.getApplicantsByStatus("interview_scheduled");
    for (OrganizationApplicant a : oList) {
      Interview interview = a.getInterview();
      ZonedDateTime now = ZonedDateTime.now();
      if (now.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime().isAfter(interview.getEndDateTime())) {
        a.setStatus("interviewed");
        organizationApplicantRepository.save(a);
      }
    }
  }
}
