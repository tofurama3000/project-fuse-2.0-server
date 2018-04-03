package server.entities.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class TimeInterval {

  public TimeInterval() {
    startDateTime = LocalDateTime.now();
    endDateTime = startDateTime.plusDays(1);
  }

  private LocalDateTime startDateTime;

  private LocalDateTime endDateTime;

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
}
