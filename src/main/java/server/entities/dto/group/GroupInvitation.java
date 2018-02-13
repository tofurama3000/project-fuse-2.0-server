package server.entities.dto.group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import server.entities.dto.User;
import server.entities.dto.group.interview.Interview;

import javax.persistence.*;

@Data
@MappedSuperclass
public abstract class GroupInvitation<T extends Group> {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "receiver_id", referencedColumnName = "id")
  private User receiver;

  @ManyToOne
  @JoinColumn(name = "sender_id", referencedColumnName = "id")
  private User sender;

  private String status;

  private String type;

  @ManyToOne
  @JoinColumn(name = "interview_id", referencedColumnName = "id")
  private Interview interview;



  @JsonIgnore
  public abstract T getGroup();

  @JsonIgnore
  public abstract GroupApplicant getApplicant();

  @JsonIgnore
  public abstract void setApplicant(GroupApplicant applicant);

  public abstract void setGroup(T group);
}
