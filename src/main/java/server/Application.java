package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import server.config.Storage;
import server.utility.ElasticsearchClient;

@SpringBootApplication
@EnableConfigurationProperties(Storage.class)
@EnableScheduling
public class Application {
  private static Logger logger = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    if (ElasticsearchClient.instance() == null)// ensure we have elasticsearch setup
      logger.error("Unable to connect to Elasticsearch! Continuing, but will retry to connect!");

    SpringApplication.run(Application.class, args);
  }

}