package server.entities.dto.group.project;


import lombok.Data;
import server.entities.dto.GroupMember;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "project_member")
@Data
public class ProjectMember extends GroupMember<Project> {
    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private Project project;

    @Override
    public void setGroup(Project group) {
        project = group;
    }

    @Override
    public Project getGroup() {
        return project;
    }
}
