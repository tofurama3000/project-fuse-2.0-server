package server.entities.dto.statistics;

import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Entity
@Immutable
@Table(name="project_organization_interview_breakdown")
public class ProjectOrganizationInterviewBreakdownView {

  @Id
  private String id;

  @Column(name="org_id")
  private Long organizationId;

  @Column(name="member_id")
  private Long memberId;

  @Column(name="member_name")
  private String member_name;

  @Column(name="proj_id")
  private Long ProjectId;

  @Column(name="proj_name")
  private String projectName;

  @Column(name="start_time")
  private LocalDateTime startTime;

  @Column(name="end_time")
  private LocalDateTime endTime;

  @Column(name="available")
  private char available;

}
