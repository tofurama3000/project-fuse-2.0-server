package server.entities.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import server.entities.dto.user.User;
import server.utility.NotificationEntityNames;

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

  public enum NotificationStatus {
    PENDING_INVITE("Pending"),
    ACCEPTED_INVITE("Accepted"),
    DECLINED_INVITE("Declined"),
    INFO("Info");

    private final String text;

    NotificationStatus(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return text;
    }
  }

  public enum NotificationType {
    JOIN_INVITATION("Invitation"),
    INTERVIEW_INVITATION("Interview"),
    APPLICATION("Applicant"),
    FRIEND_REQUEST("Request"),
    JOINED("Joined");

    private final String text;

    NotificationType(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return text;
    }
  }

  public enum NotificationEntity {
    PROJECT("Project"),
    ORGANIZATION("Organization"),
    FRIEND("Friend");

    private final String text;

    NotificationEntity(final String text) {
      this.text = text;
    }

    @Override
    public String toString() {
      return text;
    }
  }

  public enum NotificationActionState {
    DONE,
    NOT_DONE
  }

  @JsonIgnore
  public void setInfo(NotificationEntity notificationEntityType, NotificationType notificationType, NotificationStatus notificationStatus) throws IllegalArgumentException {
    NotificationEntityNames types = getNotificationEntities(notificationEntityType, notificationType, notificationStatus);
    this.data_type = types.dataType;
    this.notification_type = types.notificationType;
  }

  /**
   * This processes enums to create valid data types and notification types; it throws an IllegalArgumetnException if it
   * is unable to do so
   *
   * @param notificationEntityType - The data type for the associated entity
   * @param notificationType       - The type of the notification
   * @param notificationStatus     - The status of the associated process (eg. invitation pending, invitation accepted)
   * @return An array of String with size 2; the first element is the data type and the second element is the notification type
   * @throws IllegalArgumentException Throws an illegal argumetn exception if it is unable to generate the data type and/or notification type
   */
  @JsonIgnore
  public static NotificationEntityNames getNotificationEntities(NotificationEntity notificationEntityType, NotificationType notificationType, NotificationStatus notificationStatus) throws IllegalArgumentException {
    String notificationEntityTypeString = notificationEntityType.toString();
    String notificationTypeString = notificationType.toString();
    String notificationStatusString = notificationStatus.toString();
    String finalNotificationType = notificationEntityTypeString + notificationTypeString + ":" + notificationStatusString;
    String finalDataType = notificationEntityTypeString + notificationTypeString;

    if (isValidNotificationType(finalNotificationType) && isValidDataType(finalDataType)) {
      return new NotificationEntityNames(finalDataType, finalNotificationType);
    } else {
      if (!isValidDataType(finalDataType)) {
        throw new IllegalArgumentException("Unexpected data type " + finalDataType);
      } else {
        throw new IllegalArgumentException("Unexpected notification type " + finalNotificationType);
      }
    }
  }

  @JsonIgnore
  public void setActionState(NotificationActionState actionState) {
    this.action_done = actionState == NotificationActionState.DONE;
  }

  private static List<String> validAction = Arrays.asList(
      "done",
      "undone"
  );

  private static List<String> validNotificationTypes = Arrays.asList(

      "ProjectInvitation:Pending",
      "ProjectInvitation:Accepted",
      "ProjectInvitation:Declined",

      "ProjectInterview:Pending",
      "ProjectInterview:Accepted",
      "ProjectInterview:Declined",

      "ProjectApplicant:Info",
      "ProjectApplicant:Declined",

      "ProjectJoined:Info",

      "OrganizationInvitation:Accepted",
      "OrganizationInvitation:Pending",
      "OrganizationInvitation:Declined",

      "OrganizationInterview:Accepted",
      "OrganizationInterview:Pending",
      "OrganizationInterview:Declined",

      "OrganizationApplicant:Info",
      "OrganizationApplicant:Declined",

      "OrganizationJoined:Info",

      "FriendRequest:Pending",
      "FriendRequest:Accepted"
  );

  private static List<String> validDataTypes = Arrays.asList(
      "ProjectInvitation",
      "ProjectInterview",
      "ProjectApplicant",
      "ProjectJoined",

      "OrganizationInvitation",
      "OrganizationApplicant",
      "OrganizationInterview",
      "OrganizationJoined",

      "FriendRequest"
  );

  public static boolean isValidAction(String action) {
    return validAction.contains(action);
  }

  private static boolean isValidNotificationType(String type) {
    return validNotificationTypes.contains(type);
  }

  private static boolean isValidDataType(String type) {
    return validDataTypes.contains(type);
  }

}
