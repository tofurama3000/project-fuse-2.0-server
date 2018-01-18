package server.entities.dto;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@Table(name = "files")
public class UploadFile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "hash")
    private String hash;

    @Column(name = "mime_type")
    private String mime_type;

    @Column(name = "file_size")
    private Long file_size;

    @Column(name = "upload_time")
    private Timestamp upload_time;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

}
