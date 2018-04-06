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
@Table(name="users_with_invalid_profiles_breakdown")
public class UsersWithInvalidProfilesBreakdownView {
  @Id
  private String id;

  @Column(name="org_id")
  private Long OrganizationId;

  @Column(name="member_id")
  private Long memberId;

  @Column(name="member_name")
  private String member_name;

  @Column(name="has_thumbnail")
  private boolean hasThumbnail;

  @Column(name="has_headline")
  private boolean hasHeadline;

  @Column(name="has_summary")
  private boolean hasSummary;
}

