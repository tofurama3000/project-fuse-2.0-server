package server.entities.dto.team;

import lombok.Data;
import lombok.Getter;
import server.entities.dto.User;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "team")
@Data
public class Team {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @ManyToOne
  @JoinColumn(name = "owner_id", referencedColumnName = "id")
  @Getter
  private User owner;

  private String name;
}
