package server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.entities.dto.Link;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

public class LinkResolver {

  private static Logger logger = LoggerFactory.getLogger(LinkResolver.class);
  private static final HashSet<String> validReferenceTypes = new HashSet<>();

  static {
    validReferenceTypes.add("Project");
    validReferenceTypes.add("Organization");
    validReferenceTypes.add("User");
  }

  enum LinkType {
    YOUTUBE, OTHER
  }

  public static Link resolveLink(Link preResolvedLink) throws Exception {

    if (!linkHasValidFieldsSet(preResolvedLink)) {
      throw new Exception("Invalid fields");
    }

    URI uri;
    try {
      uri = new URI(preResolvedLink.getLink());
    } catch (URISyntaxException e) {
      logger.warn("Could not resolve link: " + e.getMessage());
      throw e;
    }

    Link link = new Link();
    link.setName(preResolvedLink.getName());
    switch (determineLinkType(uri)) {
      case YOUTUBE:
        link.setLink(getEmbeddedUrlForYoutube(uri));
        break;
      default:
        link.setLink(preResolvedLink.getLink());
    }
    link.setReferencedId(preResolvedLink.getReferencedId());
    link.setReferencedType(preResolvedLink.getReferencedType());
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
    int firstVEquals = uri.getQuery().indexOf("v=");
    String id = uri.getQuery().substring(firstVEquals + 2);
    return "https://www.youtube.com/embed/" + id;
  }

  private static boolean linkHasValidFieldsSet(Link link) {
    // all fields are not null (and non empty). Referenced type is valid
    return link.getName() != null && !link.getName().isEmpty() &&
        link.getLink() != null && !link.getLink().isEmpty() &&
        link.getReferencedId() != null && link.getReferencedId() != null &&
        validReferenceTypes.contains(link.getReferencedType());
  }
}
