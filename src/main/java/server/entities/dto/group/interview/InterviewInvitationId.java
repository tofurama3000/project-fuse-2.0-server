package server.entities.dto.group.interview;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Data
public class InterviewInvitationId {
  @Column(name = "group_id")
  private Long groupId;

  @Column(name = "group_type")
  private String groupType;
}
