package server.entities.dto.statistics;

import jdk.nashorn.internal.ir.annotations.Immutable;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Immutable
@Table(name="member_project_organization_interview_summary")
public class MemberProjectOrganizationInterviewSummaryView {
    @Id
    private String id;

    @Column(name="member_id")
    private Long memberId;

    @Column(name="member_name")
    private String member_name;

    @Column(name="org_id")
    private Long OrganizationId;

    @Column(name="num_interviews")
    private Long numberOfInterviews;

    @Column(name="num_projects_with_interviews")
    private Long numberOfProjectsWithInterviews;
}
