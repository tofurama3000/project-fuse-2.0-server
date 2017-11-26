package server.entities.dto;

import lombok.Data;
import lombok.Getter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "notification")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", referencedColumnName = "id")
    private User receiver;

    @Column(name = "message")
    private String message;

    @Column(name = "time")
    private Timestamp time;

    @Column(name = "hasRead")
    private short hasRead;
}
