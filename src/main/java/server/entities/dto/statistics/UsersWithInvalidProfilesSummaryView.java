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
@Table(name="users_with_invalid_profiles_summary")
public class UsersWithInvalidProfilesSummaryView {
  @Id
  private Long id;

  @Column(name="num_members_no_summary")
  private Long numMembersNoSummary;

  @Column(name="num_members_no_headline")
  private Long numMembersNoHeadline;

  @Column(name="num_members_no_thumbnail")
  private Long numMembersNoThumbnail;

  @Column(name="num_members")
  private Long numMembers;
}
