package server.controllers.rest;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import server.controllers.FuseSessionController;
import server.entities.dto.organization.Organization;
import server.repositories.OrganizationRepository;

@Controller
@RequestMapping(value = "/organization")
@Transactional
public class OrganizationController extends JoinableController<Organization> {

  @Autowired
  public OrganizationController(FuseSessionController fuseSessionController, OrganizationRepository organizationRepository,
                           SessionFactory sessionFactory) {
    super(fuseSessionController, sessionFactory, organizationRepository);
  }

  @Override
  protected boolean validFieldsForCreate(Organization entity) {
    return entity.getName() != null;
  }

  @Override
  protected boolean validFieldsForDelete(Organization entity) {
    return entity.getName() != null;
  }
}
