package server.entities.dto.group;

import static server.entities.Restriction.INVITE;
import static server.entities.Restriction.NONE;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import server.entities.Interviewable;
import server.entities.Restriction;
import server.entities.dto.User;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@Data
@MappedSuperclass
public abstract class Group implements Interviewable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "owner_id", referencedColumnName = "id")
  private User owner;

  private String name;

  @Column(name = "restriction")
  @JsonIgnore
  private String restrictionString;

  public Restriction getRestriction() {
    if (restrictionString != null && restrictionString.equals("INVITE")) {
      return INVITE;
    }
    return NONE;
  }

  public void setRestriction(String restriction) {
    restrictionString = restriction;
  }

}
