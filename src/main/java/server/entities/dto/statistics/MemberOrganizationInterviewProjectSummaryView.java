package server.entities.dto.statistics;

import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Immutable
@Table(name="member_organization_interview_project_summary")
public class MemberOrganizationInterviewProjectSummaryView {
    @Id
    private Long member_id;
    private String member_name;
    private Long proj_id;
    private Long org_id;
    private Long num_interviews;
}
