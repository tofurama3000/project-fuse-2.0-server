package server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import server.utility.ElasticsearchClient;

@SpringBootApplication
@EnableScheduling
public class Application {
  public static void main(String[] args) {
    if(ElasticsearchClient.instance() == null)// ensure we have elasticsearch setup
      System.err.println("Unable to connect to Elasticsearch! Continuing, but will retry to connect!");
    SpringApplication.run(Application.class, args);
  }

}