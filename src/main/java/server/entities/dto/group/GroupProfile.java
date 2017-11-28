package server.entities.dto.group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import server.entities.dto.User;
import server.entities.dto.group.Group;
import server.entities.dto.group.project.ProjectProfile;

import javax.persistence.*;

@Entity
@Data
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

    public  void merge(GroupProfile p0, GroupProfile p){
        if(p.getHeadline()!=null){
            p0.setHeadline( p.getHeadline());
        }
        if(p.getSummary()!=null){
            p0.setSummary(p.getSummary());
        }

    }

}
