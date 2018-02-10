package server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.entities.dto.Link;

import java.net.URI;
import java.net.URISyntaxException;

public class LinkResolver {

  private static Logger logger = LoggerFactory.getLogger(LinkResolver.class);

  enum LinkType {
    YOUTUBE, OTHER
  }

  public static Link resolveLink(String name, String url) throws URISyntaxException {
    URI uri;
    try {
      uri = new URI("http://support.domain.com/default.aspx?id=12345");
    } catch (URISyntaxException e) {
      logger.warn("Could not resolve link: " + e.getMessage());
      throw e;
    }

    Link link = new Link();
    link.setName(name);
    switch (determineLinkType(uri)) {
      case YOUTUBE:
        link.setLink(getEmbeddedUrlForYoutube(uri));
      default:
        link.setLink(url);
    }
    return link;
  }

  private static LinkType determineLinkType(URI uri) {
    switch (uri.getHost()) {
      case "www.youtube.com":
        return LinkType.YOUTUBE;
      default:
        return LinkType.OTHER;
    }
  }

  private static String getEmbeddedUrlForYoutube(URI uri) {
    int lastSlashIndex = uri.getPath().lastIndexOf('/');
    String id = uri.getPath().substring(lastSlashIndex);
    return  "https://www.youtube.com/embed/" + id;
  }
}
