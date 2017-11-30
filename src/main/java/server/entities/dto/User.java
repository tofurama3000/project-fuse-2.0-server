package server.entities.dto;

import static server.constants.RegistrationStatus.UNREGISTERED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "user")
@Data
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String name;

  @JsonIgnore
  private String encoded_password;

  @OneToOne
  @JoinColumn(name = "user_profile_id", referencedColumnName = "id")
  private UserProfile userProfile;

  @JsonIgnore
  @Getter(AccessLevel.NONE)
  @Transient
  private String _password;

  private String email;

  // default make users unregistered
  @Column(name = "registration_status")
  private char registrationStatus = UNREGISTERED;

  public void setRegistrationStatus(Character registrationStatus) {
    if (registrationStatus == null) {
      this.registrationStatus = UNREGISTERED;
    } else {
      this.registrationStatus = registrationStatus;
    }
  }

  private void setPassword(String password) {
    this.encoded_password = new BCryptPasswordEncoder().encode(password);
    this._password = password;
  }

  public boolean checkPassword() {
    return new BCryptPasswordEncoder().matches(this._password, this.encoded_password);
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof User && Objects.equals(((User) object).getId(), this.getId());
  }

  public int hashCode() {
    return id.hashCode();
  }
}
