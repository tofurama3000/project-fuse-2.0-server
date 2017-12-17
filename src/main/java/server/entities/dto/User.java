package server.entities.dto;

import static server.constants.RegistrationStatus.UNREGISTERED;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import server.entities.BaseIndexable;

import javax.persistence.*;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ToString(exclude = "user")
@Entity
@Table(name = "user")
@Data
public class User extends BaseIndexable {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String name;

  @JsonIgnore
  private String encoded_password;

  @JsonManagedReference
  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "user_profile_id", referencedColumnName = "id")
  private UserProfile profile;

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

  @Override
  public Map<String, Object> getEsJson() {
    Map<String, Object> map = new HashMap<>();

    map.put("id", this.id);
    map.put("name", this.name);
    map.put("email", this.email);
    if(this.profile != null)
      map.put("skills", this.profile.getSkills());
    else
      map.put("skills", "");

    return map;
  }

  @Override
  public String getEsIndex() {
    return "users";
  }

  @Override
  public String getEsType() {
    return "info";
  }

  @Override
  @JsonIgnore
  public String getEsId() {
    return this.id.toString();
  }
}
