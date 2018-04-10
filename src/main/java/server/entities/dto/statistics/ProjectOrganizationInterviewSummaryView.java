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
@Table(name="project_organization_interview_summary")
public class ProjectOrganizationInterviewSummaryView {
  @Id
  private String id;

  @Column(name="org_id")
  private Long organizationId;

  @Column(name="proj_id")
  private Long ProjectId;

  @Column(name="proj_name")
  private String projectName;

  @Column(name="total_interviews")
  private Long totalInterviews;

  @Column(name="used_interviews")
  private Long usedInterviews;
}
