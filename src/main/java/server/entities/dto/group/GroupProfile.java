package server.entities.dto.group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import server.entities.dto.User;

import javax.persistence.*;

@Entity
@MappedSuperclass
public abstract class GroupProfile<T extends Group>  {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

   private  String headline;

    private  String summary;

    @JsonIgnore
    public abstract T getGroup();

    public abstract void setGroup(T group);
}
