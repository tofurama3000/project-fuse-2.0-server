package server.entities.dto.user;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "friend")
@Data
public class Friend {
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

  public void setStatus(String status) {
    status = status.toLowerCase();
    if (ValidStatuses().indexOf(status) != -1) {
      this.status = status;
    } else {
      this.status = null;
    }
  }

  public static List<String> ValidStatuses() {
    return valid;
  }

  private static List<String> valid = java.util.Arrays.asList(
      "accepted",
      "applied",
      "deleted",
      "declined"
  );
}
