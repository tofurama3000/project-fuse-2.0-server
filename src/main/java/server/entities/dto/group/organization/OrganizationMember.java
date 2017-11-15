package server.entities.dto.group.organization;


import lombok.Data;
import lombok.Getter;
import server.entities.dto.User;
import server.entities.dto.UserToGroupRelationship;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "organization_member")
@Data
public class OrganizationMember implements UserToGroupRelationship {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "role_id")
  private int roleId;

  @ManyToOne
  @JoinColumn(name = "organization_id", referencedColumnName = "id")
  @Getter
  private Organization organization;

  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "id")
  @Getter
  private User user;

  @Override
  public long getGroupId() {
    return organization.getId();
  }
}
