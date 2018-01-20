package server.entities.dto.group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import server.entities.dto.User;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@MappedSuperclass
public abstract class GroupApplicant<T extends Group> {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @ManyToOne
  @JoinColumn(name = "sender_id", referencedColumnName = "id")
  private User sender;

  @Column(name = "status")
  private String status;

  @Column(name = "time")
  private LocalDateTime time;

  public void setTime(String dateTime) {
    ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTime);
    time = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
  }

  public void setStatus(String status) {
    status = status.toLowerCase();
    if (ValidStatuses().indexOf(status) != -1) {
      this.status = status;
    } else {
      this.status = null;
    }
  }

  @JsonIgnore
  public LocalDateTime getDateTime() {
    return time;
  }

  public String getTime() {
    return time.toString() + "+00:00";
  }

  @JsonIgnore
  public abstract T getGroup();

  public abstract void setGroup(T group);

  public static List<String> ValidStatuses() {
    return valid;
  }

  private static List<String> valid = java.util.Arrays.asList("accepted",
          "declined",
          "pending",
          "interviewed",
          "interview_scheduled");
}
