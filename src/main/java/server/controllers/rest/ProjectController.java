package server.controllers.rest;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import server.controllers.FuseSessionController;
import server.entities.dto.project.Project;
import server.repositories.ProjectRepository;

@Controller
@RequestMapping(value = "/project")
@Transactional
public class ProjectController extends GroupController<Project> {

  @Autowired
  public ProjectController(FuseSessionController fuseSessionController, ProjectRepository projectRepository,
                           SessionFactory sessionFactory) {
    super(fuseSessionController, sessionFactory, projectRepository);
  }

  @Override
  protected boolean validFieldsForCreate(Project entity) {
    return entity.getName() != null;
  }

  @Override
  protected boolean validFieldsForDelete(Project entity) {
    return entity.getName() != null;
  }

}
