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

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private String headline;

    private String summary;

    private String skills;

    public UserProfile merge(UserProfile original, UserProfile newUserProfile) {
        if (newUserProfile.getHeadline() != null) {
            original.setHeadline(newUserProfile.getHeadline());
        }
        if (newUserProfile.getSummary() != null) {
            original.setSummary(newUserProfile.getSummary());
        }
        if (newUserProfile.getSkills() != null) {
            original.setSkills(newUserProfile.getSkills());
        }
        return original;
    }
}
