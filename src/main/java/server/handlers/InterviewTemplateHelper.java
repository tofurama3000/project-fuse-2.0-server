package server.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.controllers.rest.errors.DeniedException;
import server.controllers.rest.response.BaseResponse;
import server.controllers.rest.response.GeneralResponse;
import server.entities.dto.group.organization.InterviewTemplate;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.project.Project;
import server.entities.dto.user.User;
import server.entities.user_to_group.permissions.PermissionFactory;
import server.entities.user_to_group.permissions.UserToOrganizationPermission;
import server.repositories.group.organization.OrganizationInterviewTemplateRepository;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static server.constants.RoleValue.ADMIN;
import static server.controllers.rest.response.CannedResponse.INSUFFICIENT_PRIVELAGES;

@Component
public class InterviewTemplateHelper {

    //@Autowired
   // private OrganizationInterviewTemplateRepository organizationInterviewTemplateRepository;

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
