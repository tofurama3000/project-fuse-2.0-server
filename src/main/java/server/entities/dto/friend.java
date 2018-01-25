package server.entities.dto;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "friend")
@Data
public class friend {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @ManyToOne
  @JoinColumn(name = "receiver_id", referencedColumnName = "id")
  private User receiver;

  @ManyToOne
  @JoinColumn(name = "sender_id", referencedColumnName = "id")
  private User sender;

  @Column(name = "status")
  private String status;
}
