package server.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import server.entities.BaseIndexable;
import server.repositories.UserRepository;
import server.repositories.group.organization.OrganizationRepository;
import server.repositories.group.project.ProjectRepository;

import java.util.function.Consumer;


@Service
public class ElasticsearchReindexService {

  private static final Logger logger = LoggerFactory.getLogger(ElasticsearchReindexService.class);

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private ProjectRepository projectRepository;

  private static <T extends BaseIndexable> Consumer<T> getConsumer() {
    return t -> {
      String docId = t.getEsIndex() + "/" + t.getEsType() + "/" + t.getEsId();
      if (!t.tryToIndex()) {
        logger.error("Unable to index document " + docId);
      } else {
        logger.info("Indexed doucment " + docId);
      }
    };
  }

  @Scheduled(fixedDelay = 12L * 60L * 60L * 1000L) // runs once every 12 hours; in milliseconds
  public void reindex() throws InterruptedException {
    logger.info("Starting to re-index");

    logger.info("Indexing users");

    //ElasticsearchClient.instance().ensureIndexTypeExists(User.esIndex(), User.indexDefinition());

    userRepository.findAll().forEach(getConsumer());

    projectRepository.findAll().forEach(getConsumer());

    organizationRepository.findAll().forEach(getConsumer());

    logger.info("reindex completed");
  }
}