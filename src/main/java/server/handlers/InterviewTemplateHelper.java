package server.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.entities.dto.group.organization.InterviewTemplate;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.repositories.group.organization.OrganizationInterviewTemplateRepository;


@Component
public class InterviewTemplateHelper {

    private final OrganizationInterviewTemplateRepository organizationInterviewTemplateRepository;

    @Autowired
    public InterviewTemplateHelper(OrganizationInterviewTemplateRepository organizationInterviewTemplateRepository) {
        this.organizationInterviewTemplateRepository = organizationInterviewTemplateRepository;
    }

    public void createTemplate(Project project, Organization organization, String start, String end) {
        InterviewTemplate template = new InterviewTemplate();
        template.setStart(start);
        template.setEnd(end);
        template.setOrganization(organization);
        template.setProject(project);
        organizationInterviewTemplateRepository.save(template);
    }
}
