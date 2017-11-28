package server.entities.dto;


import lombok.Data;
import server.entities.dto.group.GroupProfile;

import javax.persistence.*;

@Entity
@Table(name = "user_profile")
@Data
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private  String headline;

    private  String summary;

    private  String skills;

    public  void merge( UserProfile p0,UserProfile p){
        if(p.getHeadline()!=null){
            p0.setHeadline( p.getHeadline());
        }
        if(p.getSummary()!=null){
            p0.setSummary(p.getSummary());
        }
        if(p.getSkills()!=null){
            p0.setSkills(p.getSkills());
        }
    }
}
