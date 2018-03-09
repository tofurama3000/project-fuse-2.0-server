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
        PENDING_INVITE,
        ACCPETED_INVITE,
        DECLINED_INVITE,
        INFO
    }

    ;

    public enum NotificationType {
        JOIN_INVITATION,
        INTERVIEW_INVITATION,
        APPLICATION,
        FRIEND_REQUEST,
        JOINED
    }

    public enum NotificationEntity {
        PROJECT,
        ORGANIZATION,
        FRIEND
    }

    public enum NotificationActionState {
        DONE,
        NOT_DONE
    }

    @JsonIgnore
    public void setInfo(NotificationEntity notificationEntityType, NotificationType notificationType, NotificationStatus notificationStatus) throws IllegalArgumentException {
        String[] types = getNotificationEntities(notificationEntityType, notificationType, notificationStatus);
        this.data_type = types[0];
        this.notification_type = types[1];
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
    public static String[] getNotificationEntities(NotificationEntity notificationEntityType, NotificationType notificationType, NotificationStatus notificationStatus) throws IllegalArgumentException {
        String entityType = "";
        switch (notificationEntityType) {
            case PROJECT:
                entityType = "Project";
                break;
            case ORGANIZATION:
                entityType = "Organization";
                break;
            case FRIEND:
                entityType = "Friend";
                break;
            default:
                throw new IllegalArgumentException("Unexpected value " + notificationEntityType + " for notification entity type");
        }

        String notifType;
        switch (notificationType) {
            case JOIN_INVITATION:
                notifType = "Invitation";
                break;
            case INTERVIEW_INVITATION:
                notifType = "Interview";
                break;
            case FRIEND_REQUEST:
                notifType = "Request";
                break;
            case APPLICATION:
                notifType = "Applicant";
                break;
            case JOINED:
                notifType = "Joined";
                break;
            default:
                throw new IllegalArgumentException("Unexpected value " + notificationType + " for notification type");
        }

        String notifStatus = "";
        switch (notificationStatus) {
            case ACCPETED_INVITE:
                notifStatus = "Accepted";
                break;
            case DECLINED_INVITE:
                notifStatus = "Declined";
                break;
            case PENDING_INVITE:
                notifStatus = "Pending";
                break;
            case INFO:
                notifStatus = "Info";
                break;
        }

        String finalNotificationType = entityType + notifType + ":" + notifStatus;
        String finalDataType = entityType + notifType;

        if (isValidNotificationType(finalNotificationType) && isValidDataType(finalDataType)) {
            return new String[]{finalDataType, finalNotificationType};
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

    public static boolean isValidNotificationType(String type) {
        return validNotificationTypes.contains(type);
    }

    public static boolean isValidDataType(String type) {
        return validDataTypes.contains(type);
    }

}
