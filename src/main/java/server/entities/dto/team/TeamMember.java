package server.entities.dto.team;


import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "team_member")
@Data
public class TeamMember {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  private int team_id;

  private int user_id;
}
