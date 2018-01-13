package server.entities.dto;


import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

@ToString(exclude = "user")
@Entity
@Table(name = "user_profile")
@Data
public class UserProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @OneToOne
  @JsonBackReference
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  private User user;

  private String headline;

  private String summary;

  private String skills;

  public UserProfile merge(UserProfile original, UserProfile newUserProfile) {
    if (newUserProfile.getHeadline() != null) {
      original.setHeadline(newUserProfile.getHeadline());
    }
    if (newUserProfile.getSummary() != null) {
      original.setSummary(newUserProfile.getSummary());
    }
    if (newUserProfile.getSkills() != null) {
      original.setSkills(newUserProfile.getSkills());
    }
    return original;
  }
}