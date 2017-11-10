package example.dto;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Transient;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity // This tells Hibernate to make a table out of this class
@Table(name = "user")
@Data
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  private String name;

  @JsonIgnore
  private String encoded_password;

  @JsonIgnore
  @Transient
  private String _password;

  private String email;

  private void setPassword(String password){
    this.encoded_password = new BCryptPasswordEncoder().encode(password);
    this._password = password;
  }

  public boolean checkPassword(){
    return new BCryptPasswordEncoder().matches(this._password, this.encoded_password);
  }
}
