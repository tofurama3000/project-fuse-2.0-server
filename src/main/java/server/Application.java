package server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import server.utility.ElasticsearchClient;

@SpringBootApplication
public class Application {
  public static void main(String[] args) {
    if(ElasticsearchClient.instance() != null)// ensure we have elasticsearch setup
      SpringApplication.run(Application.class, args);
  }
}