package server.entities.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.Transient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
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

  @JsonIgnore
  @Transient
  @Getter(AccessLevel.NONE)
  private String _password;

  private String email;

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
