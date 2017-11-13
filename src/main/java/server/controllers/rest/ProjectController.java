package server.controllers.rest;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import server.controllers.FuseSessionController;
import server.entities.dto.Project;
import server.entities.dto.User;
import server.repositories.ProjectRepository;

import java.util.List;

public class ProjectController extends JoinableController<Project> {

  @Autowired
  private SessionFactory sessionFactory;

  @Autowired
  public ProjectController(FuseSessionController fuseSessionController, ProjectRepository projectRepository, SessionFactory sessionFactory) {
    super(fuseSessionController, sessionFactory, projectRepository);
  }

  @Override
  protected boolean validFieldsForCreate(Project entity) {
    return false;
  }

  @Override
  protected boolean validFieldsForDelete(Project entity) {
    return false;
  }

}
