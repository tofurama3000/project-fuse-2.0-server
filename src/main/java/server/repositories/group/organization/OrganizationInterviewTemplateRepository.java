package server.repositories.group.organization;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.group.organization.InterviewTemplate;

@Transactional
public interface OrganizationInterviewTemplateRepository extends CrudRepository<InterviewTemplate, Long>{
}
