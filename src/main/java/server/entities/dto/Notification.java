package server.entities.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import server.entities.dto.user.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "notification")
@Data
public class Notification {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @ManyToOne
  @JoinColumn(name = "receiver_id", referencedColumnName = "id")
  private User receiver;

  @Column(name = "message")
  private String message;

  @Column(name = "object_type")
  private String objectType;

  @Column(name = "object_id")
  private Long objectId;

  @Column(name = "time")
  private LocalDateTime time;

  @Column(name = "has_read")
  private boolean hasRead;

  @Column(name = "deleted")
  private boolean deleted;

  @Transient
  private Object data;

  public void setTime(String dateTime) {
    ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTime);
    time = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
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

  private static List<String> validTypes = Arrays.asList(
          "ProjectApplicant",
          "ProjectInvitation",
          "ProjectInterview:Invite",
          "ProjectInvitation:Accepted",
          "OrganizationInterview:Invite",
          "OrganizationInvitation",
          "OrganizationApplicant",
          "Friend:Request",
          "Friend:Accepted"
  );

  public static boolean isValidType(String type) {
    return validTypes.contains(type);
  }
}
