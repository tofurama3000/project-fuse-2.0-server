package server.entities.dto.group.interview;

import lombok.Getter;
import lombok.Setter;
import server.entities.dto.group.Group;
import server.entities.dto.group.GroupInvitation;

import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

@Getter
@Table(name = "interview_invitation")
public class InterviewInvitation extends GroupInvitation {

  @Setter
  @JoinColumn(name = "interview_id", referencedColumnName = "id")
  private InterviewTimeSlot interviewTimeSlot;

  private InterviewInvitationId id;

  @Transient
  private Group group;

  private String groupName;

  @Override
  public void setGroup(Group group) {
    this.group = group;
    id.setGroupId(group.getId());
    id.setGroupType(group.getGroupType());
    groupName = getGroupName();
  }
}
