package server.entities.dto;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "link")
@Data
public class Link {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "link")
  private String link;

  @Column(name = "referenced_type")
  private String referencedType;

  @Column(name = "referenced_Id")
  private Long referencedId;

  public String getImg() {
    // Images are picked on the server in case we want the server (Tier 3 feature)
    //  to cache images for the actual page (such as media tags) in the future
    // By having images decided here we can change where images come from
    //  without having to affect the UI

    // All relative links are relative to the UI URL and not the server
    switch (name.toLowerCase()) {
      case "github":
        return "/assets/images/github.svg";
      case "youtube":
        return "/assets/images/youtube.svg";
      case "linkedin":
        return "/assets/images/linkedin.svg";
      case "facebook":
        return "/assets/images/facebook.svg";
      case "twitter":
        return "/assets/images/twitter.svg";
      case "wordpress":
        return "/assets/images/wordpress.svg";
      case "drupal":
        return "/assets/images/drupal.svg";
      default:
        return null;
    }
  }
}