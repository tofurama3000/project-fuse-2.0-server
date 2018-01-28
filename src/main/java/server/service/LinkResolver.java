package server.service;

import org.elasticsearch.common.inject.Singleton;
import server.entities.dto.Link;

@Singleton
public class LinkResolver {

  public Link resolveLink(String name, String url) {
    return new Link();
  }
}
