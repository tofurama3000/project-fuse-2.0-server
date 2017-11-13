package server.entities.dto.organization;

import lombok.Data;
import lombok.Getter;
import server.entities.Group;
import server.entities.dto.User;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "organization")
@Data
public class Organization implements Group {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  private String name;

  @ManyToOne
  @JoinColumn(name = "owner_id", referencedColumnName = "id")
  @Getter
  private User owner;

  @Override
  public String getTableName() {
    return "Organization";
  }
}
