package server.entities.dto;

import lombok.Data;
import server.entities.Joinable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "project")
@Data
public class Project implements Joinable {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  private String name;

  private User owner;

  @Override
  public String getTableName() {
    return "Project";
  }
}
