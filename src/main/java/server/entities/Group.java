package server.entities;

import static server.entities.Restriction.INVITE;
import static server.entities.Restriction.NONE;
import lombok.Data;
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
public abstract class Group {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @ManyToOne
  @JoinColumn(name = "owner_id", referencedColumnName = "id")
  private User owner;

  private String name;

  @Column(name = "restriction")
  private String restrictionString;

  public Restriction getRestriction() {
    if (restrictionString != null && restrictionString.equals("INVITE")) {
      return INVITE;
    }
    return NONE;
  }

  public abstract String getTableName();

  public abstract String getRelationshipTableName();

}
