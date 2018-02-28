package server.entities;

import lombok.Data;
import server.entities.dto.group.Group;
import server.entities.dto.user.User;
import server.entities.user_to_group.permissions.UserToGroupPermission;

import java.util.HashSet;
import java.util.Set;

import static server.constants.RoleValue.*;

@Data
public class MemberRelationship {
    private User user;

    private Set<String> roles;

    public MemberRelationship(){
        user = null;
        roles = null;
    }

    public MemberRelationship(User user) {
        this.user = user;
        roles = null;
    }

    public <T extends Group> void setPermissions(UserToGroupPermission<T> permissions){
        roles = new HashSet<>();
        permissions.getRoles().forEach(
                role -> {
                    switch(role) {
                        case DEFAULT_USER:
                            roles.add("Default");
                            break;
                        case INVITED_TO_JOIN:
                            roles.add("Can_Join");
                            break;
                        case ADMIN:
                            roles.add("Admin");
                            break;
                        case OWNER:
                            roles.add("Owner");
                            break;
                        case INVITED_TO_INTERVIEW:
                            roles.add("Can_Interview");
                            break;
                        case TO_INTERVIEW:
                            roles.add("To_Interview");
                            break;
                        case CREATE_PROJECT_IN_ORGANIZATION:
                            roles.add("Can_Create_Projects");
                            break;
                    }
                });
    }
}
