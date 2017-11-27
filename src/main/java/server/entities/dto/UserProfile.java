package server.entities.dto;


import lombok.Data;

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
}
