package server.controllers.rest;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import server.controllers.SessionController;
import server.entities.dto.User;
import server.entities.dto.team.Team;
import server.repositories.TeamRespository;

import java.util.List;

@Controller
@RequestMapping(value = "/team")
@Transactional
public class TeamController extends JoinableController<Team> {

  @Autowired
  private SessionFactory sessionFactory;

  public TeamController(@Autowired SessionController sessionController, @Autowired TeamRespository teamRespository) {
    super(sessionController, teamRespository);
  }

  @Override
  protected boolean validFieldsForCreate(Team entity) {
    return entity.getName() != null;
  }

  @Override
  protected boolean validFieldsForDelete(Team entity) {
    return entity.getName() != null;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected List<Team> getEntitiesWith(User owner, String name) {
    Query query = sessionFactory.getCurrentSession()
        .createQuery("FROM Team t WHERE t.owner = :owner AND t.name = :name");

    query.setParameter("owner", owner);
    query.setParameter("name", name);

    return query.list();
  }
}
