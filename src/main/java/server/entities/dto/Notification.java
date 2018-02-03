package server.entities.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import server.repositories.UserRepository;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
