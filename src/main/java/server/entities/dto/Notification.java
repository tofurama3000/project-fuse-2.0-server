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

  @Column(name = "notification_type")
  private String notification_type;

  @Column(name = "data_type")
  private String data_type;

  @Column(name = "object_id")
  private Long objectId;

  @Column(name = "time")
  private LocalDateTime time;

  @Column(name = "has_read")
  private boolean hasRead;

  @Column(name = "action_done")
  private boolean action_done;

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
  private  static List<String> validAction = Arrays.asList(
      "done",
      "undone"
  );

  private static List<String> validNotificationTypes = Arrays.asList(

          "ProjectInvitation:Invite",
          "ProjectInvitation:Accepted",
          "ProjectInvitation:Declined",
          "ProjectApplicant",
          "ProjectApplicant:Accepted",
          "ProjectApplicant:Declined",


          "OrganizationInvitation:Accepted",
          "OrganizationInvitation:Invite",
          "OrganizationInvitation:Declined",
          "OrganizationApplicant",
          "OrganizationApplicant:Accepted",
          "OrganizationApplicant:Declined",
          "Friend:Request",
          "Friend:Accepted"
  );

  private static List<String> validDataTypes = Arrays.asList(
      "ProjectInvitation",
      "ProjectApplicant",

      "OrganizationInvitation",
      "OrganizationApplicant",

      "Friend"
  );

  public static boolean isValidAction(String action) {
    return validAction.contains(action);
  }
  public static boolean isValidNotificationType (String type) {
    return validNotificationTypes.contains(type);
  }
  public static boolean isValidDataType (String type) {
    return validDataTypes.contains(type);
  }

}
