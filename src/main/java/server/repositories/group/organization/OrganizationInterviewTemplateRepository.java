package server.repositories.group.organization;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.group.organization.InterviewTemplate;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.user.User;

import java.time.LocalDateTime;
import java.util.List;

@Transactional
public interface OrganizationInterviewTemplateRepository extends CrudRepository<InterviewTemplate, Long>{

    @Query("From InterviewTemplate t WHERE t.organization =:organization AND t.startDateTime>:now")
    List<InterviewTemplate> getInterviewTemplatesByStart(@Param("organization") Organization organization,
                                                         @Param("now") LocalDateTime now);
}
