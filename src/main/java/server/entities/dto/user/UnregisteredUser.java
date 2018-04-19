package server.entities.dto.user;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "unregistered_user")
public class UnregisteredUser {
  @Id
  @Column(name = "user_id")
  private Long userId;

  private String registrationKey;
}
