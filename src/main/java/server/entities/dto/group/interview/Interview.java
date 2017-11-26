package server.entities.dto.group.interview;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Table(name = "interview_time_slot")
public class InterviewTimeSlot {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private LocalDateTime startDateTime;

  private LocalDateTime endDateTime;

  // How to represent end time (just another datetime, or do we just include length?)

}
