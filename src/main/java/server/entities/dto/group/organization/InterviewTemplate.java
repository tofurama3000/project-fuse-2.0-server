package server.entities.dto.group.organization;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Entity
@Table(name = "interview_template")
public class InterviewTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "start_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_time")
    private LocalDateTime endDateTime;


    @ManyToOne
    @JoinColumn(name = "organization_id", referencedColumnName = "id")
    private Organization organization;


    public void setStart(String dateTime) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTime);
        startDateTime = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public void setEnd(String dateTime) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTime);
        endDateTime = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    @JsonIgnore
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    @JsonIgnore
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public String getStart() {
        return startDateTime != null ? startDateTime.toString() + "+00:00" : null;
    }

    public String getEnd() {
        return endDateTime != null ? endDateTime.toString() + "+00:00" : null;
    }

    public String getPrettyFormattedTimeInterval() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, hh:mm A");
        String startString = formatter.format(startDateTime);
        String endString = formatter.format(endDateTime);

        return startString + " to " + endString;
    }

}
