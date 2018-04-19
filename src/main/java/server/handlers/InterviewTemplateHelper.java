package server.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.entities.dto.group.interview.Interview;
import server.entities.dto.group.organization.InterviewTemplate;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.repositories.group.InterviewRepository;
import server.repositories.group.organization.OrganizationInterviewTemplateRepository;
import server.repositories.group.organization.OrganizationRepository;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;


@Component
public class InterviewTemplateHelper {

    private final OrganizationInterviewTemplateRepository organizationInterviewTemplateRepository;

    private final OrganizationRepository organizationRepository;

    private final InterviewRepository interviewRepository;

    @Autowired
    public InterviewTemplateHelper(OrganizationInterviewTemplateRepository organizationInterviewTemplateRepository,
                                   OrganizationRepository organizationRepository,
                                   InterviewRepository interviewRepository) {
        this.organizationInterviewTemplateRepository = organizationInterviewTemplateRepository;
        this.organizationRepository = organizationRepository;
        this.interviewRepository = interviewRepository;
    }

    public void createTemplate(Organization organization, String start, String end) {
        InterviewTemplate template = new InterviewTemplate();
        template.setStart(start);
        template.setEnd(end);
        template.setOrganization(organization);
        organizationInterviewTemplateRepository.save(template);
        List<Project> projects = organizationRepository.getAllProjectsByOrganization(organization);
        for(Project project : projects)
        {
            Interview interview = new Interview();
            interview.setGroupType("Project");
            interview.setGroupId(project.getId());
            interview.setUser(null);
            interview.setStartDateTime(template.getStartDateTime());
            interview.setEndDateTime(template.getEndDateTime());
            interviewRepository.save(interview);
        }
    }

    public List<InterviewTemplate> getAllTemplates(Organization organization)
    {
        return organizationInterviewTemplateRepository.getInterviewTemplatesByStart(organization,
                ZonedDateTime.now(ZoneOffset.UTC).toLocalDateTime());
    }
}
