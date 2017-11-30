package server.entities.dto.group.team;


import lombok.Data;
import server.entities.dto.GroupMember;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "team_member")
@Data
public class TeamMember extends GroupMember<Team> {

    @ManyToOne
    @JoinColumn(name = "team_id", referencedColumnName = "id")
    private Team team;

    @Override
    public void setGroup(Team group) {
        team = group;
    }

    @Override
    public Team getGroup() {
        return team;
    }
}
