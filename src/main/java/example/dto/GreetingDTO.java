package example.dto;

import example.Greeting;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class GreetingDTO {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  private String content;

  public void setFields(Greeting greeting) {
    content = greeting.getContent();
  }

  public String getContents() {
    return content;
  }
}
