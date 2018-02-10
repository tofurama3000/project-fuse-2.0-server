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
    this.time = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
  }

  public void setStatus(String status) {
    status = status.toLowerCase();
    if (IsValidStatus(status)) {
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
    if (time != null) {
      return time.toString() + "+00:00";
    }
    return "";
  }

  @JsonIgnore
  public abstract T getGroup();

  public abstract void setGroup(T group);

  public static List<String> ValidStatuses() {
    return validStatuses;
  }

  private static List<String> validStatuses = java.util.Arrays.asList(
      "accepted",
      "pending",
      "interviewed",
      "interview_scheduled",
      "invited",
      "declined"
  );

  public static boolean IsValidStatus(String status) {
    return validStatuses.contains(status);
  }

  public static Integer GetStatusOrder(String status) {
    final int index = validStatuses.indexOf(status.toLowerCase());
    return (index >= 0) ? index : validStatuses.size();
  }
}
