package server.controllers.rest;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import server.controllers.FuseSessionController;
import server.entities.dto.team.Team;
import server.repositories.TeamRespository;

@Controller
@RequestMapping(value = "/team")
@Transactional
public class TeamController extends JoinableController<Team> {

  @Autowired
  private SessionFactory sessionFactory;

  @Autowired
  public TeamController(FuseSessionController fuseSessionController, TeamRespository teamRespository, SessionFactory sessionFactory) {
    super(fuseSessionController, sessionFactory, teamRespository);
  }

  @Override
  protected boolean validFieldsForCreate(Team entity) {
    return entity.getName() != null;
  }

  @Override
  protected boolean validFieldsForDelete(Team entity) {
    return entity.getName() != null;
  }


}
