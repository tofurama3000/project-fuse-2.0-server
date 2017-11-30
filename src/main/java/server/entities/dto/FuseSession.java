package server.entities.dto;

import lombok.Data;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "session")
@Data
public class FuseSession {

    public FuseSession(String sessionId, User sessionFor) {
        this.user = sessionFor;
        this.sessionId = sessionId;
        this.created = new Timestamp(System.currentTimeMillis());
    }

    public FuseSession() {
    }

    @Getter
    @Id
    @Column(name = "session_id")
    private String sessionId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @Getter
    private User user;

    @Getter
    private Timestamp created;

    @Override
    public String toString() {
        return this.sessionId;
    }
}
