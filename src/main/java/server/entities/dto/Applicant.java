package server.entities.dto;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "team_applicant")
@Data
public class Applicant {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    private User sender;

  @Column(name = "status")
    private String status;

  @Column(name = "team_id")
  private long team_id;

  @Column(name = "time")
  private Timestamp time;
  

}
