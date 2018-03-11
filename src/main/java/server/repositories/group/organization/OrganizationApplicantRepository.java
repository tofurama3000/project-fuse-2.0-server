package server.repositories.group.organization;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import server.entities.dto.group.organization.Organization;
import server.entities.dto.group.organization.OrganizationApplication;
import server.repositories.group.GroupApplicantRepository;

import java.util.List;

@Transactional
public interface OrganizationApplicantRepository extends GroupApplicantRepository<OrganizationApplication, Organization> {

  @Query("FROM OrganizationApplication a where a.organization = :organization and a.status = :status ")
  List<OrganizationApplication> getApplicants(@Param("organization") Organization organization, @Param("status") String status);

  @Query("FROM OrganizationApplication a where a.status = :status ")
  List<OrganizationApplication> getApplicantsByStatus(@Param("status") String status);
}
